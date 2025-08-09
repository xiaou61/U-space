package com.xiaou.auth.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaou.auth.admin.domain.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户角色关联Mapper接口
 * 
 * @author xiaou
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    /**
     * 根据用户ID删除用户角色关联
     * 
     * @param userId   用户ID
     * @param userType 用户类型
     * @return 删除数量
     */
    int deleteByUserId(@Param("userId") String userId, @Param("userType") String userType);

    /**
     * 根据角色ID删除用户角色关联
     * 
     * @param roleId 角色ID
     * @return 删除数量
     */
    int deleteByRoleId(@Param("roleId") String roleId);

    /**
     * 批量插入用户角色关联
     * 
     * @param userRoles 用户角色关联列表
     * @return 插入数量
     */
    int batchInsert(@Param("userRoles") List<SysUserRole> userRoles);

    /**
     * 根据用户ID查询角色ID列表
     * 
     * @param userId   用户ID
     * @param userType 用户类型
     * @return 角色ID列表
     */
    List<String> selectRoleIdsByUserId(@Param("userId") String userId, @Param("userType") String userType);

    /**
     * 查询所有用户及其角色信息
     * 
     * @return 用户角色关联列表
     */
    List<SysUserRole> selectAllUserRoles();
}