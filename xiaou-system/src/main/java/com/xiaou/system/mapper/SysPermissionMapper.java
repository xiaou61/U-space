package com.xiaou.system.mapper;

import com.xiaou.system.domain.SysPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 权限Mapper接口
 *
 * @author xiaou
 */
@Mapper
public interface SysPermissionMapper {

    /**
     * 根据ID查询权限
     */
    SysPermission selectById(@Param("id") Long id);

    /**
     * 根据权限编码查询权限
     */
    SysPermission selectByPermissionCode(@Param("permissionCode") String permissionCode);

    /**
     * 查询权限列表
     */
    List<SysPermission> selectList(@Param("permission") SysPermission permission);

    /**
     * 查询权限总数
     */
    Long selectCount(@Param("permission") SysPermission permission);

    /**
     * 根据管理员ID查询权限列表
     */
    List<SysPermission> selectPermissionsByAdminId(@Param("adminId") Long adminId);

    /**
     * 根据角色ID查询权限列表
     */
    List<SysPermission> selectPermissionsByRoleId(@Param("roleId") Long roleId);

    /**
     * 查询所有权限树
     */
    List<SysPermission> selectPermissionTree();

    /**
     * 根据父ID查询子权限列表
     */
    List<SysPermission> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 新增权限
     */
    int insert(@Param("permission") SysPermission permission);

    /**
     * 修改权限
     */
    int update(@Param("permission") SysPermission permission);

    /**
     * 根据ID删除权限
     */
    int deleteById(@Param("id") Long id);

    /**
     * 批量删除权限
     */
    int deleteByIds(@Param("ids") List<Long> ids);

    /**
     * 更新状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 检查权限编码是否存在
     */
    int checkPermissionCodeExists(@Param("permissionCode") String permissionCode, @Param("excludeId") Long excludeId);

    /**
     * 检查是否有子权限
     */
    int checkHasChildren(@Param("parentId") Long parentId);
} 