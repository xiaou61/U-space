package com.xiaou.auth.admin.controller;

import com.xiaou.auth.admin.domain.entity.SysPermission;
import com.xiaou.auth.admin.service.SysPermissionService;
import com.xiaou.common.domain.R;
import com.xiaou.log.annotation.Log;
import com.xiaou.log.enums.BusinessType;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 菜单权限管理Controller
 * 
 * @author xiaou
 */
@RestController
@RequestMapping("/admin/menu")
@Validated
public class MenuManagementController {

    @Resource
    private SysPermissionService sysPermissionService;

    /**
     * 获取所有权限列表
     */
    @GetMapping("/list")
    public R<List<SysPermission>> getAllPermissions() {
        return sysPermissionService.getAllPermissions();
    }

    /**
     * 获取权限树（用于菜单管理）
     */
    @GetMapping("/tree")
    public R<List<SysPermission>> getPermissionTree() {
        return sysPermissionService.getMenuPermissionTree();
    }

    /**
     * 根据权限ID获取权限详情
     */
    @GetMapping("/{permissionId}")
    public R<SysPermission> getPermissionById(@PathVariable String permissionId) {
        return sysPermissionService.getPermissionById(permissionId);
    }

    /**
     * 根据父权限ID获取子权限列表
     */
    @GetMapping("/children")
    public R<List<SysPermission>> getChildPermissions(@RequestParam(required = false) String parentId) {
        return sysPermissionService.getChildPermissions(parentId);
    }

    /**
     * 添加权限
     */
    @Log(title = "添加菜单权限", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public R<String> addPermission(@RequestBody SysPermission permission) {
        // 参数验证
        if (permission.getPermissionCode() == null || permission.getPermissionCode().trim().isEmpty()) {
            return R.fail("权限代码不能为空");
        }
        if (permission.getPermissionName() == null || permission.getPermissionName().trim().isEmpty()) {
            return R.fail("权限名称不能为空");
        }
        if (permission.getPermissionType() == null || permission.getPermissionType().trim().isEmpty()) {
            return R.fail("权限类型不能为空");
        }

        return sysPermissionService.addPermission(permission);
    }

    /**
     * 更新权限
     */
    @Log(title = "更新菜单权限", businessType = BusinessType.UPDATE)
    @PutMapping("/update")
    public R<String> updatePermission(@RequestBody SysPermission permission) {
        if (permission.getId() == null || permission.getId().trim().isEmpty()) {
            return R.fail("权限ID不能为空");
        }
        if (permission.getPermissionCode() == null || permission.getPermissionCode().trim().isEmpty()) {
            return R.fail("权限代码不能为空");
        }
        if (permission.getPermissionName() == null || permission.getPermissionName().trim().isEmpty()) {
            return R.fail("权限名称不能为空");
        }

        return sysPermissionService.updatePermission(permission);
    }

    /**
     * 删除权限
     */
    @Log(title = "删除菜单权限", businessType = BusinessType.DELETE)
    @DeleteMapping("/delete/{permissionId}")
    public R<String> deletePermission(@PathVariable String permissionId) {
        if (permissionId == null || permissionId.trim().isEmpty()) {
            return R.fail("权限ID不能为空");
        }
        return sysPermissionService.deletePermission(permissionId);
    }

    /**
     * 批量删除权限
     */
    @Log(title = "批量删除菜单权限", businessType = BusinessType.DELETE)
    @DeleteMapping("/batch-delete")
    public R<String> batchDeletePermissions(@RequestBody List<String> permissionIds) {
        if (permissionIds == null || permissionIds.isEmpty()) {
            return R.fail("权限ID列表不能为空");
        }
        return sysPermissionService.batchDeletePermissions(permissionIds);
    }

    /**
     * 更新权限排序
     */
    @Log(title = "更新权限排序", businessType = BusinessType.UPDATE)
    @PutMapping("/sort")
    public R<String> updatePermissionSort(@RequestBody Map<String, Object> params) {
        String permissionId = (String) params.get("permissionId");
        Integer sortOrder = (Integer) params.get("sortOrder");

        if (permissionId == null || permissionId.trim().isEmpty()) {
            return R.fail("权限ID不能为空");
        }
        if (sortOrder == null) {
            return R.fail("排序号不能为空");
        }

        return sysPermissionService.updatePermissionSort(permissionId, sortOrder);
    }
}