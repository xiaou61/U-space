package com.xiaou.auth.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.auth.admin.domain.entity.SysRole;
import com.xiaou.common.domain.R;

import java.util.List;

/**
 * 系统角色Service接口
 * 
 * @author xiaou
 */
public interface SysRoleService extends IService<SysRole> {

    /**
     * 查询所有角色
     * 
     * @return 角色列表
     */
    R<List<SysRole>> getAllRoles();

    /**
     * 根据角色代码查询角色
     * 
     * @param roleCode 角色代码
     * @return 角色信息
     */
    R<SysRole> getRoleByCode(String roleCode);

    /**
     * 添加角色
     * 
     * @param role 角色信息
     * @return 操作结果
     */
    R<String> addRole(SysRole role);

    /**
     * 更新角色
     * 
     * @param role 角色信息
     * @return 操作结果
     */
    R<String> updateRole(SysRole role);

    /**
     * 删除角色
     * 
     * @param roleId 角色ID
     * @return 操作结果
     */
    R<String> deleteRole(String roleId);

    /**
     * 根据用户ID查询用户角色列表
     * 
     * @param userId   用户ID
     * @param userType 用户类型
     * @return 角色列表
     */
    R<List<SysRole>> getUserRoles(String userId, String userType);

    /**
     * 为用户分配角色
     * 
     * @param userId    用户ID
     * @param roleCodes 角色代码列表
     * @param userType  用户类型
     * @return 操作结果
     */
    R<String> assignRolesToUser(String userId, List<String> roleCodes, String userType);
}