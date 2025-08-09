package com.xiaou.auth.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaou.auth.admin.domain.entity.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统权限Mapper接口
 * 
 * @author xiaou
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    /**
     * 根据角色ID查询权限列表
     * 
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<SysPermission> selectPermissionsByRoleId(@Param("roleId") String roleId);

    /**
     * 根据用户ID查询用户权限列表
     * 
     * @param userId   用户ID
     * @param userType 用户类型
     * @return 权限列表
     */
    List<SysPermission> selectPermissionsByUserId(@Param("userId") String userId, @Param("userType") String userType);

    /**
     * 查询所有启用的权限（用于构建权限树）
     * 
     * @return 权限列表
     */
    List<SysPermission> selectEnabledPermissions();

    /**
     * 根据权限代码查询权限
     * 
     * @param permissionCode 权限代码
     * @return 权限信息
     */
    SysPermission selectByPermissionCode(@Param("permissionCode") String permissionCode);

    /**
     * 查询菜单类型的权限列表
     * 
     * @return 菜单权限列表
     */
    List<SysPermission> selectMenuPermissions();

    /**
     * 根据用户ID查询权限代码列表
     * 
     * @param userId   用户ID
     * @param userType 用户类型
     * @return 权限代码列表
     */
    List<String> selectPermissionCodesByUserId(@Param("userId") String userId, @Param("userType") String userType);
}