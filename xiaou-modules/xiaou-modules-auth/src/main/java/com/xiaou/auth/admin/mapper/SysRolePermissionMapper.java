package com.xiaou.auth.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaou.auth.admin.domain.entity.SysRolePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色权限关联Mapper接口
 * 
 * @author xiaou
 */
@Mapper
public interface SysRolePermissionMapper extends BaseMapper<SysRolePermission> {

    /**
     * 根据角色ID删除角色权限关联
     * 
     * @param roleId 角色ID
     * @return 删除数量
     */
    int deleteByRoleId(@Param("roleId") String roleId);

    /**
     * 根据权限ID删除角色权限关联
     * 
     * @param permissionId 权限ID
     * @return 删除数量
     */
    int deleteByPermissionId(@Param("permissionId") String permissionId);

    /**
     * 批量插入角色权限关联
     * 
     * @param rolePermissions 角色权限关联列表
     * @return 插入数量
     */
    int batchInsert(@Param("rolePermissions") List<SysRolePermission> rolePermissions);

    /**
     * 根据角色ID查询权限ID列表
     * 
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    List<String> selectPermissionIdsByRoleId(@Param("roleId") String roleId);
}