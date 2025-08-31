package com.xiaou.system.mapper;

import com.xiaou.system.domain.SysAdmin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理员用户Mapper接口
 *
 * @author xiaou
 */
@Mapper
public interface SysAdminMapper {

    /**
     * 根据ID查询管理员
     */
    SysAdmin selectById(@Param("id") Long id);

    /**
     * 根据用户名查询管理员
     */
    SysAdmin selectByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询管理员
     */
    SysAdmin selectByEmail(@Param("email") String email);

    /**
     * 查询管理员列表
     */
    List<SysAdmin> selectList(@Param("admin") SysAdmin admin);

    /**
     * 查询管理员总数
     */
    Long selectCount(@Param("admin") SysAdmin admin);

    /**
     * 新增管理员
     */
    int insert(@Param("admin") SysAdmin admin);

    /**
     * 修改管理员
     */
    int update(@Param("admin") SysAdmin admin);

    /**
     * 根据ID删除管理员
     */
    int deleteById(@Param("id") Long id);

    /**
     * 批量删除管理员
     */
    int deleteByIds(@Param("ids") List<Long> ids);

    /**
     * 更新最后登录信息
     */
    int updateLastLoginInfo(@Param("id") Long id, 
                           @Param("lastLoginTime") LocalDateTime lastLoginTime, 
                           @Param("lastLoginIp") String lastLoginIp, 
                           @Param("loginCount") Integer loginCount);

    /**
     * 更新密码
     */
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    /**
     * 更新状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 检查用户名是否存在
     */
    int checkUsernameExists(@Param("username") String username, @Param("excludeId") Long excludeId);

    /**
     * 检查邮箱是否存在
     */
    int checkEmailExists(@Param("email") String email, @Param("excludeId") Long excludeId);
} 