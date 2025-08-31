package com.xiaou.system.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.xiaou.common.core.domain.ResultCode;
import com.xiaou.common.exception.BusinessException;
import com.xiaou.system.domain.SysAdmin;
import com.xiaou.system.domain.SysLoginLog;
import com.xiaou.system.dto.LoginRequest;
import com.xiaou.system.dto.LoginResponse;
import com.xiaou.system.dto.UpdateAdminRequest;
import com.xiaou.system.dto.ChangePasswordRequest;
import com.xiaou.system.mapper.SysAdminMapper;
import com.xiaou.system.mapper.SysLoginLogMapper;
import com.xiaou.system.mapper.SysPermissionMapper;
import com.xiaou.system.mapper.SysRoleMapper;
import com.xiaou.common.security.JwtTokenUtil;
import com.xiaou.system.service.SysAdminService;
import com.xiaou.common.security.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员服务实现类
 *
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysAdminServiceImpl implements SysAdminService {

    private final SysAdminMapper adminMapper;
    private final SysRoleMapper roleMapper;
    private final SysPermissionMapper permissionMapper;
    private final SysLoginLogMapper loginLogMapper;
    private final JwtTokenUtil jwtTokenUtil;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginResponse login(LoginRequest loginRequest) {
        // 获取请求信息
        HttpServletRequest request = getHttpServletRequest();
        String ip = getIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        UserAgent ua = UserAgentUtil.parse(userAgent);

        // 创建登录日志
        SysLoginLog loginLog = new SysLoginLog();
        loginLog.setUsername(loginRequest.getUsername());
        loginLog.setLoginIp(ip);
        loginLog.setBrowser(ua.getBrowser().getName());
        loginLog.setOs(ua.getOs().getName());
        loginLog.setLoginTime(LocalDateTime.now());

        try {
            // 1. 验证用户存在性
            SysAdmin admin = adminMapper.selectByUsername(loginRequest.getUsername());
            if (admin == null) {
                log.warn("❌ 登录失败");
                log.warn("原因: 用户不存在");
                log.warn("用户: {}", loginRequest.getUsername());
                loginLog.setLoginStatus(1);
                loginLog.setLoginMessage("用户不存在");
                loginLogMapper.insert(loginLog);
                throw new BusinessException(ResultCode.LOGIN_FAILED, "用户名或密码错误");
            }

            loginLog.setAdminId(admin.getId());

            // 2. 验证用户状态
            if (admin.getStatus() == 1) {
                log.warn("❌ 登录失败");
                log.warn("原因: 用户已被禁用");
                log.warn("用户: {}", loginRequest.getUsername());
                loginLog.setLoginStatus(1);
                loginLog.setLoginMessage("用户已被禁用");
                loginLogMapper.insert(loginLog);
                throw new BusinessException(ResultCode.ACCOUNT_DISABLED, "用户已被禁用");
            }


            // 3. 验证密码
            if (!passwordEncoder.matches(loginRequest.getPassword(), admin.getPassword())) {
                log.warn("❌ 登录失败");
                log.warn("原因: 密码错误");
                log.warn("用户: {}", loginRequest.getUsername());
                loginLog.setLoginStatus(1);
                loginLog.setLoginMessage("密码错误");
                loginLogMapper.insert(loginLog);
                throw new BusinessException(ResultCode.LOGIN_FAILED, "用户名或密码错误");
            }

            // 4. 生成JWT令牌
            String accessToken = jwtTokenUtil.generateAccessToken(admin.getUsername(), admin.getId());
            String refreshToken = jwtTokenUtil.generateRefreshToken(admin.getUsername(), admin.getId());

            // 5. 更新登录信息
            Integer loginCount = admin.getLoginCount() == null ? 1 : admin.getLoginCount() + 1;
            adminMapper.updateLastLoginInfo(admin.getId(), LocalDateTime.now(), ip, loginCount);
            
            // 6. 保存Token到Redis
            admin.setLastLoginTime(LocalDateTime.now());
            admin.setLastLoginIp(ip);
            admin.setLoginCount(loginCount);
            try {
                String adminJson = objectMapper.writeValueAsString(admin);
                tokenService.saveToken(admin.getUsername(), accessToken, adminJson);
            } catch (Exception e) {
                log.error("保存管理员Token失败", e);
                throw new BusinessException(ResultCode.ERROR, "登录失败");
            }

            // 7. 记录登录成功日志
            loginLog.setLoginStatus(0);
            loginLog.setLoginMessage("登录成功");
            loginLogMapper.insert(loginLog);

            // 8. 构建响应
            LoginResponse response = new LoginResponse();
            response.setAccessToken(accessToken)
                    .setRefreshToken(refreshToken)
                    .setExpiresIn(7200L); // 2小时

            // 9. 构建用户信息
            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
            userInfo.setId(admin.getId())
                    .setUsername(admin.getUsername())
                    .setRealName(admin.getRealName())
                    .setEmail(admin.getEmail())
                    .setAvatar(admin.getAvatar())
                    .setLastLoginTime(admin.getLastLoginTime())
                    .setRoles(getAdminRoles(admin.getId()))
                    .setPermissions(getAdminPermissions(admin.getId()));

            response.setUserInfo(userInfo);

            log.info("🎉 登录成功");
            log.info("用户: {}", loginRequest.getUsername());
            return response;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("登录异常", e);
            loginLog.setLoginStatus(1);
            loginLog.setLoginMessage("系统异常");
            loginLogMapper.insert(loginLog);
            throw new BusinessException(ResultCode.ERROR, "登录失败，请稍后重试");
        }
    }

    @Override
    public SysAdmin getById(Long id) {
        if (id == null) {
            return null;
        }
        return adminMapper.selectById(id);
    }

    @Override
    public SysAdmin getByUsername(String username) {
        if (StrUtil.isBlank(username)) {
            return null;
        }
        return adminMapper.selectByUsername(username);
    }

    @Override
    public List<SysAdmin> list(SysAdmin admin) {
        return adminMapper.selectList(admin);
    }

    @Override
    public List<SysAdmin> page(SysAdmin admin, Integer pageNum, Integer pageSize) {
        // 这里可以集成分页插件，暂时返回全部
        return adminMapper.selectList(admin);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(SysAdmin admin) {
        // 检查用户名是否已存在
        if (checkUsernameExists(admin.getUsername(), null)) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "用户名已存在");
        }

        // 检查邮箱是否已存在
        if (StrUtil.isNotBlank(admin.getEmail()) && checkEmailExists(admin.getEmail(), null)) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "邮箱已存在");
        }

        // 加密密码
        if (StrUtil.isNotBlank(admin.getPassword())) {
            admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        }

        // 设置默认值
        if (admin.getStatus() == null) {
            admin.setStatus(0); // 正常状态
        }
        if (admin.getGender() == null) {
            admin.setGender(0); // 未知性别
        }
        if (admin.getLoginCount() == null) {
            admin.setLoginCount(0);
        }

        return adminMapper.insert(admin) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(SysAdmin admin) {
        if (admin.getId() == null) {
            throw new BusinessException(ResultCode.PARAM_VALIDATE_ERROR, "管理员ID不能为空");
        }

        // 检查邮箱是否已存在
        if (StrUtil.isNotBlank(admin.getEmail()) && checkEmailExists(admin.getEmail(), admin.getId())) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "邮箱已存在");
        }

        return adminMapper.update(admin) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Long id) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_VALIDATE_ERROR, "管理员ID不能为空");
        }
        return adminMapper.deleteById(id) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_VALIDATE_ERROR, "管理员ID列表不能为空");
        }
        return adminMapper.deleteByIds(ids) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updatePassword(Long id, String oldPassword, String newPassword) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_VALIDATE_ERROR, "管理员ID不能为空");
        }

        SysAdmin admin = adminMapper.selectById(id);
        if (admin == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "管理员不存在");
        }

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, admin.getPassword())) {
            throw new BusinessException(ResultCode.PARAM_VALIDATE_ERROR, "原密码错误");
        }

        // 加密新密码
        String encryptedPassword = passwordEncoder.encode(newPassword);
        return adminMapper.updatePassword(id, encryptedPassword) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPassword(Long id, String newPassword) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_VALIDATE_ERROR, "管理员ID不能为空");
        }

        // 加密新密码
        String encryptedPassword = passwordEncoder.encode(newPassword);
        return adminMapper.updatePassword(id, encryptedPassword) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateStatus(Long id, Integer status) {
        if (id == null) {
            throw new BusinessException(ResultCode.PARAM_VALIDATE_ERROR, "管理员ID不能为空");
        }
        return adminMapper.updateStatus(id, status) > 0;
    }

    @Override
    public boolean checkUsernameExists(String username, Long excludeId) {
        if (StrUtil.isBlank(username)) {
            return false;
        }
        return adminMapper.checkUsernameExists(username, excludeId) > 0;
    }

    @Override
    public boolean checkEmailExists(String email, Long excludeId) {
        if (StrUtil.isBlank(email)) {
            return false;
        }
        return adminMapper.checkEmailExists(email, excludeId) > 0;
    }

    @Override
    public List<String> getAdminRoles(Long adminId) {
        if (adminId == null) {
            return List.of();
        }
        return roleMapper.selectRolesByAdminId(adminId)
                .stream()
                .map(role -> role.getRoleCode())
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getAdminPermissions(Long adminId) {
        if (adminId == null) {
            return List.of();
        }
        return permissionMapper.selectPermissionsByAdminId(adminId)
                .stream()
                .map(permission -> permission.getPermissionCode())
                .collect(Collectors.toList());
    }

    /**
     * 获取HttpServletRequest
     */
    private HttpServletRequest getHttpServletRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * 获取客户端IP地址
     */
    private String getIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // 如果是多个IP，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCurrentUserInfo(Long currentUserId, UpdateAdminRequest request) {
        if (currentUserId == null) {
            throw new BusinessException(ResultCode.PARAM_VALIDATE_ERROR, "用户ID不能为空");
        }

        SysAdmin currentAdmin = adminMapper.selectById(currentUserId);
        if (currentAdmin == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "用户不存在");
        }

        // 检查邮箱是否被其他用户使用
        if (StrUtil.isNotBlank(request.getEmail()) && 
            checkEmailExists(request.getEmail(), currentUserId)) {
            throw new BusinessException(ResultCode.DATA_ALREADY_EXIST, "邮箱已被其他用户使用");
        }

        // 创建更新对象
        SysAdmin updateAdmin = new SysAdmin();
        updateAdmin.setId(currentUserId);
        updateAdmin.setRealName(request.getRealName());
        updateAdmin.setEmail(request.getEmail());
        updateAdmin.setPhone(request.getPhone());
        updateAdmin.setAvatar(request.getAvatar());
        updateAdmin.setGender(request.getGender());
        updateAdmin.setRemark(request.getRemark());
        updateAdmin.setUpdateTime(LocalDateTime.now());
        updateAdmin.setUpdateBy(currentUserId);

        log.info("🔄 更新用户信息");
        log.info("用户ID: {}", currentUserId);
        log.info("更新内容: {}", request);
        
        return adminMapper.update(updateAdmin) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changeCurrentUserPassword(Long currentUserId, ChangePasswordRequest request) {
        if (currentUserId == null) {
            throw new BusinessException(ResultCode.PARAM_VALIDATE_ERROR, "用户ID不能为空");
        }

        // 验证确认密码
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ResultCode.PARAM_VALIDATE_ERROR, "新密码与确认密码不一致");
        }

        SysAdmin currentAdmin = adminMapper.selectById(currentUserId);
        if (currentAdmin == null) {
            throw new BusinessException(ResultCode.DATA_NOT_EXIST, "用户不存在");
        }

        // 验证原密码
        if (!passwordEncoder.matches(request.getOldPassword(), currentAdmin.getPassword())) {
            throw new BusinessException(ResultCode.PARAM_VALIDATE_ERROR, "原密码错误");
        }

        // 新密码不能与原密码相同
        if (passwordEncoder.matches(request.getNewPassword(), currentAdmin.getPassword())) {
            throw new BusinessException(ResultCode.PARAM_VALIDATE_ERROR, "新密码不能与原密码相同");
        }

        // 加密新密码
        String encryptedPassword = passwordEncoder.encode(request.getNewPassword());
        
        log.info("🔐 修改用户密码");
        log.info("用户ID: {}", currentUserId);
        log.info("用户名: {}", currentAdmin.getUsername());
        
        return adminMapper.updatePassword(currentUserId, encryptedPassword) > 0;
    }
} 