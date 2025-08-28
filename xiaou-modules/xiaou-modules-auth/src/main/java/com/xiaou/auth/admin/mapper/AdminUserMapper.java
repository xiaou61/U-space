package com.xiaou.auth.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaou.auth.admin.domain.entity.AdminUser;
import com.xiaou.auth.admin.domain.resp.AdminUserWithRolesResp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface AdminUserMapper extends BaseMapper<AdminUser> {

    /**
     * 查询所有管理员用户及其角色信息
     * 
     * @return 用户角色信息列表
     */
    List<AdminUserWithRolesResp> selectUsersWithRoles();

    /**
     * 根据用户ID查询用户角色
     * 
     * @param userId 用户ID
     * @return 角色代码列表
     */
    List<String> selectUserRoleCodes(@Param("userId") String userId);
}
