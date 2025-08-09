package com.xiaou.auth.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.auth.admin.domain.entity.SysRole;
import com.xiaou.auth.admin.domain.entity.SysUserRole;
import com.xiaou.auth.admin.mapper.SysRoleMapper;
import com.xiaou.auth.admin.mapper.SysUserRoleMapper;
import com.xiaou.auth.admin.service.SysRoleService;
import com.xiaou.common.domain.R;
import com.xiaou.satoken.utils.LoginHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统角色Service实现类
 * 
 * @author xiaou
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private LoginHelper loginHelper;

    @Override
    public R<List<SysRole>> getAllRoles() {
        try {
            List<SysRole> roles = sysRoleMapper.selectEnabledRoles();
            return R.ok(roles);
        } catch (Exception e) {
            return R.fail("查询角色列表失败: " + e.getMessage());
        }
    }

    @Override
    public R<SysRole> getRoleByCode(String roleCode) {
        try {
            if (!StringUtils.hasText(roleCode)) {
                return R.fail("角色代码不能为空");
            }
            SysRole role = sysRoleMapper.selectByRoleCode(roleCode);
            return R.ok(role);
        } catch (Exception e) {
            return R.fail("查询角色失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> addRole(SysRole role) {
        try {
            // 验证角色代码是否已存在
            SysRole existRole = sysRoleMapper.selectByRoleCode(role.getRoleCode());
            if (existRole != null) {
                return R.fail("角色代码已存在");
            }

            // 设置创建人
            role.setCreateBy(loginHelper.getCurrentAppUserId());
            role.setIsSystem(false); // 非系统角色
            role.setStatus(true); // 默认启用

            int result = sysRoleMapper.insert(role);
            return result > 0 ? R.ok("添加成功") : R.fail("添加失败");
        } catch (Exception e) {
            return R.fail("添加角色失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> updateRole(SysRole role) {
        try {
            // 检查是否为系统角色
            SysRole existRole = sysRoleMapper.selectById(role.getId());
            if (existRole == null) {
                return R.fail("角色不存在");
            }
            if (existRole.getIsSystem()) {
                return R.fail("系统角色不能修改");
            }

            // 设置更新人
            role.setUpdateBy(loginHelper.getCurrentAppUserId());

            int result = sysRoleMapper.updateById(role);
            return result > 0 ? R.ok("更新成功") : R.fail("更新失败");
        } catch (Exception e) {
            return R.fail("更新角色失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> deleteRole(String roleId) {
        try {
            // 检查是否为系统角色
            SysRole existRole = sysRoleMapper.selectById(roleId);
            if (existRole == null) {
                return R.fail("角色不存在");
            }
            if (existRole.getIsSystem()) {
                return R.fail("系统角色不能删除");
            }

            // 检查是否有用户使用该角色
            LambdaQueryWrapper<SysUserRole> userRoleQuery = new LambdaQueryWrapper<>();
            userRoleQuery.eq(SysUserRole::getRoleId, roleId);
            Long userCount = sysUserRoleMapper.selectCount(userRoleQuery);
            if (userCount > 0) {
                return R.fail("该角色下有用户，不能删除");
            }

            int result = sysRoleMapper.deleteById(roleId);
            return result > 0 ? R.ok("删除成功") : R.fail("删除失败");
        } catch (Exception e) {
            return R.fail("删除角色失败: " + e.getMessage());
        }
    }

    @Override
    public R<List<SysRole>> getUserRoles(String userId, String userType) {
        try {
            List<SysRole> roles = sysRoleMapper.selectRolesByUserId(userId, userType);
            return R.ok(roles);
        } catch (Exception e) {
            return R.fail("查询用户角色失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<String> assignRolesToUser(String userId, List<String> roleCodes, String userType) {
        try {
            if (userId == null || userId.trim().isEmpty()) {
                return R.fail("用户ID不能为空");
            }

            // 先删除用户原有角色
            sysUserRoleMapper.deleteByUserId(userId, userType);

            // 如果角色列表不为空，则插入新角色
            if (roleCodes != null && !roleCodes.isEmpty()) {
                List<SysUserRole> userRoles = new ArrayList<>();

                for (String roleCode : roleCodes) {
                    // 根据角色代码查找角色ID
                    SysRole role = sysRoleMapper.selectByRoleCode(roleCode);
                    if (role != null) {
                        SysUserRole userRole = new SysUserRole();
                        userRole.setUserId(userId);
                        userRole.setRoleId(role.getId()); // 使用角色ID，不是角色代码
                        userRole.setUserType(userType);
                        // ID和created_at会在SQL中自动生成
                        userRoles.add(userRole);
                    }
                }

                if (!userRoles.isEmpty()) {
                    int result = sysUserRoleMapper.batchInsert(userRoles);
                    return result > 0 ? R.ok("分配角色成功") : R.fail("分配角色失败");
                }
            }

            return R.ok("角色分配成功");
        } catch (Exception e) {
            return R.fail("分配角色失败: " + e.getMessage());
        }
    }
}