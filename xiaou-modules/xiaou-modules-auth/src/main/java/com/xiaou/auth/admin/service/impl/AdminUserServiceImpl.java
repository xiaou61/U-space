package com.xiaou.auth.admin.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.auth.admin.constant.AdminUserConstant;
import com.xiaou.auth.admin.domain.entity.AdminUser;
import com.xiaou.auth.admin.domain.req.AdminUserReq;
import com.xiaou.auth.admin.mapper.AdminUserMapper;
import com.xiaou.auth.admin.mapper.SysUserRoleMapper;
import com.xiaou.auth.admin.service.AdminUserService;
import com.xiaou.common.domain.R;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.common.utils.PasswordUtil;
import com.xiaou.common.utils.StringUtils;
import com.xiaou.satoken.utils.LoginHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class AdminUserServiceImpl extends ServiceImpl<AdminUserMapper, AdminUser>
        implements AdminUserService {
    @Resource
    private LoginHelper loginHelper;

    @Resource
    private SysUserRoleMapper sysUserRoleMapper;

    @Override
    public R<SaResult> login(AdminUserReq req) {
        // 用户登录操作
        AdminUser user = MapstructUtils.convert(req, AdminUser.class);
        // 判断用户是否存在
        QueryWrapper<AdminUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(AdminUserConstant.USERNAME, user.getUsername());
        AdminUser adminUser = this.getOne(queryWrapper);
        if (adminUser == null) {
            return R.fail("用户不存在");
        }
        // 存在继续校验密码
        String password = PasswordUtil.getEncryptPassword(user.getPassword());
        if (!StringUtils.equals(password, adminUser.getPassword())) {
            return R.fail("密码错误");
        }
        // 都成功的话使用sa-token进行登录
        StpUtil.login(adminUser.getId());
        return R.ok(SaResult.data(StpUtil.getTokenInfo()));
    }

    @Override
    public R<String> updatePassword(String oldPassword, String newPassword) {
        AdminUser adminUser = this.getById(loginHelper.getCurrentAppUserId());
        if (!StringUtils.equals(PasswordUtil.getEncryptPassword(oldPassword), adminUser.getPassword())) {
            return R.fail("旧密码错误");
        }
        adminUser.setPassword(PasswordUtil.getEncryptPassword(newPassword));
        baseMapper.updateById(adminUser);
        // 删除当前登录用户
        loginHelper.removeCurrentAppUser();
        return R.ok("修改密码成功");
    }

    @Override
    public R<List<AdminUser>> getAllUsers() {
        try {
            List<AdminUser> users = this.list();
            // 清除密码信息，不返回给前端
            users.forEach(user -> user.setPassword(null));
            return R.ok(users);
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            return R.fail("获取用户列表失败: " + e.getMessage());
        }
    }

    @Override
    public R<String> addUser(AdminUser adminUser) {
        try {
            // 检查用户名是否已存在
            QueryWrapper<AdminUser> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq(AdminUserConstant.USERNAME, adminUser.getUsername());
            if (this.getOne(queryWrapper) != null) {
                return R.fail("用户名已存在");
            }

            // 加密密码
            adminUser.setPassword(PasswordUtil.getEncryptPassword(adminUser.getPassword()));

            // 保存用户
            boolean success = this.save(adminUser);
            return success ? R.ok("添加用户成功") : R.fail("添加用户失败");
        } catch (Exception e) {
            log.error("添加用户失败", e);
            return R.fail("添加用户失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> deleteUser(String userId) {
        try {
            // 检查用户是否存在
            AdminUser user = this.getById(userId);
            if (user == null) {
                return R.fail("用户不存在");
            }

            // 防止删除当前登录用户
            String currentUserId = loginHelper.getCurrentAppUserId();
            if (StringUtils.equals(userId, currentUserId)) {
                return R.fail("不能删除当前登录用户");
            }

            // 删除用户的角色关联
            sysUserRoleMapper.deleteByUserId(userId, "ADMIN");

            // 删除用户
            boolean success = this.removeById(userId);
            return success ? R.ok("删除用户成功") : R.fail("删除用户失败");
        } catch (Exception e) {
            log.error("删除用户失败", e);
            return R.fail("删除用户失败: " + e.getMessage());
        }
    }

    @Override
    public R<String> resetPassword(String userId, String newPassword) {
        try {
            AdminUser user = this.getById(userId);
            if (user == null) {
                return R.fail("用户不存在");
            }

            // 加密新密码
            user.setPassword(PasswordUtil.getEncryptPassword(newPassword));

            // 更新密码
            boolean success = this.updateById(user);
            return success ? R.ok("重置密码成功") : R.fail("重置密码失败");
        } catch (Exception e) {
            log.error("重置密码失败", e);
            return R.fail("重置密码失败: " + e.getMessage());
        }
    }

    @Override
    public R<String> updateUser(AdminUser adminUser) {
        try {
            AdminUser existingUser = this.getById(adminUser.getId());
            if (existingUser == null) {
                return R.fail("用户不存在");
            }

            // 检查用户名是否被其他用户使用
            QueryWrapper<AdminUser> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq(AdminUserConstant.USERNAME, adminUser.getUsername())
                    .ne("id", adminUser.getId());
            if (this.getOne(queryWrapper) != null) {
                return R.fail("用户名已被其他用户使用");
            }

            // 只更新用户名，不更新密码
            existingUser.setUsername(adminUser.getUsername());

            boolean success = this.updateById(existingUser);
            return success ? R.ok("更新用户成功") : R.fail("更新用户失败");
        } catch (Exception e) {
            log.error("更新用户失败", e);
            return R.fail("更新用户失败: " + e.getMessage());
        }
    }
}
