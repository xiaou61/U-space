package com.xiaou.auth.admin.controller;

import com.xiaou.auth.admin.domain.entity.SysRole;
import com.xiaou.auth.admin.domain.entity.SysPermission;
import com.xiaou.auth.admin.service.SysRoleService;
import com.xiaou.auth.admin.service.SysPermissionService;
import com.xiaou.auth.admin.mapper.AdminUserMapper;
import com.xiaou.common.domain.R;
import com.xiaou.log.annotation.Log;
import com.xiaou.log.enums.BusinessType;
import com.xiaou.satoken.utils.LoginHelper;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Arrays;
import com.xiaou.auth.admin.mapper.AdminUserMapper;

/**
 * 角色权限管理Controller
 * 
 * @author xiaou
 */
@RestController
@RequestMapping("/admin")
@Validated
public class RolePermissionController {

    @Resource
    private SysRoleService sysRoleService;

    @Resource
    private SysPermissionService sysPermissionService;

    @Resource
    private LoginHelper loginHelper;

    @Resource
    private AdminUserMapper adminUserMapper;

    // ==================== 角色管理 ====================

    /**
     * 获取所有角色
     */
    @GetMapping("/role/list")
    public R<List<SysRole>> getRoleList() {
        return sysRoleService.getAllRoles();
    }

    /**
     * 添加角色
     */
    @Log(title = "添加角色", businessType = BusinessType.INSERT)
    @PostMapping("/role/add")
    public R<String> addRole(@RequestBody SysRole role) {
        return sysRoleService.addRole(role);
    }

    /**
     * 更新角色
     */
    @Log(title = "更新角色", businessType = BusinessType.UPDATE)
    @PutMapping("/role/update")
    public R<String> updateRole(@RequestBody SysRole role) {
        return sysRoleService.updateRole(role);
    }

    /**
     * 删除角色
     */
    @Log(title = "删除角色", businessType = BusinessType.DELETE)
    @DeleteMapping("/role/delete")
    public R<String> deleteRole(@RequestParam String roleCode) {
        if (roleCode == null || roleCode.trim().isEmpty()) {
            return R.fail("角色代码不能为空");
        }

        // 根据角色代码查找角色
        R<SysRole> roleResult = sysRoleService.getRoleByCode(roleCode);
        if (roleResult.getCode() != 200 || roleResult.getData() == null) {
            return R.fail("角色不存在");
        }

        return sysRoleService.deleteRole(roleResult.getData().getId());
    }

    // ==================== 权限管理 ====================

    /**
     * 获取菜单权限树
     */
    @GetMapping("/permission/menu")
    public R<List<SysPermission>> getMenuPermissions() {
        return sysPermissionService.getMenuPermissionTree();
    }

    /**
     * 根据角色获取权限
     */
    @GetMapping("/role/permissions")
    public R<List<SysPermission>> getRolePermissions(@RequestParam String roleCode) {
        if (roleCode == null || roleCode.trim().isEmpty()) {
            return R.fail("角色代码不能为空");
        }

        // 根据角色代码查找角色
        R<SysRole> roleResult = sysRoleService.getRoleByCode(roleCode);
        if (roleResult.getCode() != 200 || roleResult.getData() == null) {
            return R.fail("角色不存在");
        }

        return sysPermissionService.getPermissionsByRoleId(roleResult.getData().getId());
    }

    /**
     * 更新角色权限
     */
    @Log(title = "更新角色权限", businessType = BusinessType.UPDATE)
    @PutMapping("/role/permissions")
    public R<String> updateRolePermissions(@RequestBody Map<String, Object> params) {
        String roleCode = (String) params.get("roleCode");
        @SuppressWarnings("unchecked")
        List<String> permissions = (List<String>) params.get("permissions");

        if (roleCode == null || roleCode.trim().isEmpty()) {
            return R.fail("角色代码不能为空");
        }

        // 根据角色代码查找角色
        R<SysRole> roleResult = sysRoleService.getRoleByCode(roleCode);
        if (roleResult.getCode() != 200 || roleResult.getData() == null) {
            return R.fail("角色不存在");
        }

        return sysPermissionService.assignPermissionsToRole(roleResult.getData().getId(), permissions);
    }

    // ==================== 用户角色管理 ====================

    /**
     * 获取用户列表及其角色信息
     */
    @GetMapping("/user/with-roles")
    public R<List<Map<String, Object>>> getUsersWithRoles() {
        try {
            // 从数据库查询管理员用户及其角色信息
            List<Map<String, Object>> userList = adminUserMapper.selectUsersWithRoles();

            // 处理角色字符串，转换为List格式
            for (Map<String, Object> user : userList) {
                String rolesStr = (String) user.get("roles");
                if (rolesStr != null && !rolesStr.isEmpty()) {
                    String[] rolesArray = rolesStr.split(",");
                    user.put("roles", Arrays.asList(rolesArray));
                } else {
                    user.put("roles", new ArrayList<>());
                }
            }

            return R.ok(userList);
        } catch (Exception e) {
            return R.fail("获取用户列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户角色
     */
    @GetMapping("/user/roles")
    public R<List<SysRole>> getUserRoles(@RequestParam String userId) {
        return sysRoleService.getUserRoles(userId, "ADMIN");
    }

    /**
     * 更新用户角色
     */
    @Log(title = "更新用户角色", businessType = BusinessType.UPDATE)
    @PutMapping("/user/roles")
    public R<String> updateUserRoles(@RequestBody Map<String, Object> params) {
        String userId = (String) params.get("userId");
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) params.get("roles");

        if (userId == null || userId.trim().isEmpty()) {
            return R.fail("用户ID不能为空");
        }

        return sysRoleService.assignRolesToUser(userId, roles, "ADMIN");
    }

    // ==================== 权限验证相关 ====================

    /**
     * 获取当前用户权限代码列表
     */
    @GetMapping("/user/permissions")
    public R<List<String>> getCurrentUserPermissions() {
        String userId = loginHelper.getCurrentAppUserId();
        System.out.println("获取用户权限 - 当前用户ID: " + userId);
        R<List<String>> result = sysPermissionService.getUserPermissionCodes(userId, "ADMIN");
        System.out.println("获取用户权限 - 权限列表: " + result.getData());
        return result;
    }

    /**
     * 获取用户菜单权限
     */
    @GetMapping("/user/menu-permissions")
    public R<List<SysPermission>> getUserMenuPermissions() {
        String userId = loginHelper.getCurrentAppUserId();
        return sysPermissionService.getPermissionsByUserId(userId, "ADMIN");
    }
}