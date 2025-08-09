package com.xiaou.auth.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaou.auth.admin.domain.entity.SysRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统角色Mapper接口
 * 
 * @author xiaou
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 根据用户ID查询用户角色列表
     * 
     * @param userId   用户ID
     * @param userType 用户类型
     * @return 角色列表
     */
    List<SysRole> selectRolesByUserId(@Param("userId") String userId, @Param("userType") String userType);

    /**
     * 根据角色代码查询角色
     * 
     * @param roleCode 角色代码
     * @return 角色信息
     */
    SysRole selectByRoleCode(@Param("roleCode") String roleCode);

    /**
     * 查询所有启用的角色
     * 
     * @return 角色列表
     */
    List<SysRole> selectEnabledRoles();
}