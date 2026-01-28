package com.xiaou.user.mapper;

import com.xiaou.user.domain.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户信息Mapper接口
 *
 * @author xiaou
 */
@Mapper
public interface UserInfoMapper {

    /**
     * 根据ID查询用户
     */
    UserInfo selectById(@Param("id") Long id);

    /**
     * 根据用户名查询用户
     */
    UserInfo selectByUsername(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     */
    UserInfo selectByEmail(@Param("email") String email);

    /**
     * 根据手机号查询用户
     */
    UserInfo selectByPhone(@Param("phone") String phone);

    /**
     * 根据用户名或邮箱查询用户（用于登录）
     */
    UserInfo selectByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);

    /**
     * 查询用户列表
     */
    List<UserInfo> selectList(@Param("user") UserInfo user);

    /**
     * 查询用户总数
     */
    Long selectCount(@Param("user") UserInfo user);

    /**
     * 新增用户
     */
    int insert(@Param("user") UserInfo user);

    /**
     * 修改用户
     */
    int update(@Param("user") UserInfo user);

    /**
     * 根据ID删除用户
     */
    int deleteById(@Param("id") Long id);

    /**
     * 批量删除用户
     */
    int deleteByIds(@Param("ids") List<Long> ids);

    /**
     * 更新最后登录信息
     */
    int updateLastLoginInfo(@Param("id") Long id, 
                           @Param("lastLoginTime") LocalDateTime lastLoginTime, 
                           @Param("lastLoginIp") String lastLoginIp);

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

    /**
     * 检查手机号是否存在
     */
    int checkPhoneExists(@Param("phone") String phone, @Param("excludeId") Long excludeId);
    
    /**
     * 批量查询用户
     * @param ids 用户ID列表
     * @return 用户列表
     */
    List<UserInfo> selectBatchIds(@Param("ids") List<Long> ids);
}
