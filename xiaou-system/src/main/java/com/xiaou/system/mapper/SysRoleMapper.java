package com.xiaou.system.mapper;

import com.xiaou.system.domain.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色Mapper接口
 *
 * @author xiaou
 */
@Mapper
public interface SysRoleMapper {

    /**
     * 根据ID查询角色
     */
    SysRole selectById(@Param("id") Long id);

    /**
     * 根据角色编码查询角色
     */
    SysRole selectByRoleCode(@Param("roleCode") String roleCode);

    /**
     * 查询角色列表
     */
    List<SysRole> selectList(@Param("role") SysRole role);

    /**
     * 查询角色总数
     */
    Long selectCount(@Param("role") SysRole role);

    /**
     * 根据管理员ID查询角色列表
     */
    List<SysRole> selectRolesByAdminId(@Param("adminId") Long adminId);

    /**
     * 新增角色
     */
    int insert(@Param("role") SysRole role);

    /**
     * 修改角色
     */
    int update(@Param("role") SysRole role);

    /**
     * 根据ID删除角色
     */
    int deleteById(@Param("id") Long id);

    /**
     * 批量删除角色
     */
    int deleteByIds(@Param("ids") List<Long> ids);

    /**
     * 更新状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 检查角色编码是否存在
     */
    int checkRoleCodeExists(@Param("roleCode") String roleCode, @Param("excludeId") Long excludeId);
} 