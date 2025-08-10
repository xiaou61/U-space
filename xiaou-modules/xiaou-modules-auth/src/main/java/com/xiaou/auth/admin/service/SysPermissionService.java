package com.xiaou.auth.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.auth.admin.domain.entity.SysPermission;
import com.xiaou.common.domain.R;

import java.util.List;

/**
 * 系统权限服务接口
 * 
 * @author xiaou
 */
public interface SysPermissionService extends IService<SysPermission> {

    /**
     * 获取菜单权限树
     * 
     * @return 权限树列表
     */
    R<List<SysPermission>> getMenuPermissionTree();

    /**
     * 根据角色ID获取权限列表
     * 
     * @param roleId 角色ID
     * @return 权限列表
     */
    R<List<SysPermission>> getPermissionsByRoleId(String roleId);

    /**
     * 根据用户ID获取权限列表
     * 
     * @param userId   用户ID
     * @param userType 用户类型
     * @return 权限列表
     */
    R<List<SysPermission>> getPermissionsByUserId(String userId, String userType);

    /**
     * 为角色分配权限
     * 
     * @param roleId        角色ID
     * @param permissionIds 权限ID列表
     * @return 操作结果
     */
    R<String> assignPermissionsToRole(String roleId, List<String> permissionIds);

    /**
     * 添加权限
     * 
     * @param permission 权限信息
     * @return 操作结果
     */
    R<String> addPermission(SysPermission permission);

    /**
     * 更新权限
     * 
     * @param permission 权限信息
     * @return 操作结果
     */
    R<String> updatePermission(SysPermission permission);

    /**
     * 删除权限
     * 
     * @param permissionId 权限ID
     * @return 操作结果
     */
    R<String> deletePermission(String permissionId);

    /**
     * 获取用户权限代码列表
     * 
     * @param userId   用户ID
     * @param userType 用户类型
     * @return 权限代码列表
     */
    R<List<String>> getUserPermissionCodes(String userId, String userType);

    // ==================== 菜单管理新增方法 ====================

    /**
     * 获取所有权限列表（用于菜单管理）
     * 
     * @return 权限列表
     */
    R<List<SysPermission>> getAllPermissions();

    /**
     * 根据权限ID获取权限详情
     * 
     * @param permissionId 权限ID
     * @return 权限详情
     */
    R<SysPermission> getPermissionById(String permissionId);

    /**
     * 批量删除权限
     * 
     * @param permissionIds 权限ID列表
     * @return 操作结果
     */
    R<String> batchDeletePermissions(List<String> permissionIds);

    /**
     * 更新权限排序
     * 
     * @param permissionId 权限ID
     * @param sortOrder    排序号
     * @return 操作结果
     */
    R<String> updatePermissionSort(String permissionId, Integer sortOrder);

    /**
     * 根据父权限ID获取子权限列表
     * 
     * @param parentId 父权限ID
     * @return 子权限列表
     */
    R<List<SysPermission>> getChildPermissions(String parentId);
}