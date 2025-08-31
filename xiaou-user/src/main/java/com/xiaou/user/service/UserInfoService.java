package com.xiaou.user.service;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.user.domain.UserInfo;
import com.xiaou.user.dto.*;

import java.util.List;

/**
 * 用户信息服务接口
 *
 * @author xiaou
 */
public interface UserInfoService {

    /**
     * 用户注册
     *
     * @param request 注册请求
     * @return 注册结果
     */
    UserInfo register(UserRegisterRequest request);

    /**
     * 用户登录
     *
     * @param request 登录请求
     * @param clientIp 客户端IP
     * @return 登录响应
     */
    UserLoginResponse login(UserLoginRequest request, String clientIp);

    /**
     * 根据ID查询用户
     *
     * @param id 用户ID
     * @return 用户信息
     */
    UserInfo getUserById(Long id);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    UserInfo getUserByUsername(String username);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户信息
     */
    UserInfo getUserByEmail(String email);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @param excludeId 排除的用户ID
     * @return 是否存在
     */
    boolean isUsernameExists(String username, Long excludeId);

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @param excludeId 排除的用户ID
     * @return 是否存在
     */
    boolean isEmailExists(String email, Long excludeId);

    /**
     * 检查手机号是否存在
     *
     * @param phone 手机号
     * @param excludeId 排除的用户ID
     * @return 是否存在
     */
    boolean isPhoneExists(String phone, Long excludeId);

    /**
     * 更新用户最后登录信息
     *
     * @param userId 用户ID
     * @param clientIp 客户端IP
     */
    void updateLastLoginInfo(Long userId, String clientIp);

    /**
     * 获取用户详细信息（返回DTO格式）
     *
     * @param userId 用户ID
     * @return 用户信息响应DTO
     */
    UserInfoResponse getUserInfoById(Long userId);

    /**
     * 更新用户信息
     *
     * @param userId 用户ID
     * @param request 更新请求
     * @return 更新后的用户信息
     */
    UserInfoResponse updateUserInfo(Long userId, UserUpdateRequest request);

    /**
     * 修改用户密码
     *
     * @param userId 用户ID
     * @param request 修改密码请求
     */
    void changePassword(Long userId, UserChangePasswordRequest request);

    /**
     * 分页查询用户列表（管理员接口）
     *
     * @param request 查询条件
     * @return 分页结果
     */
    PageResult<UserInfoResponse> getUserList(UserQueryRequest request);

    /**
     * 管理员创建用户
     *
     * @param request 创建用户请求
     * @param adminId 管理员ID
     * @return 创建的用户信息
     */
    UserInfoResponse createUser(AdminCreateUserRequest request, Long adminId);

    /**
     * 删除用户（逻辑删除）
     *
     * @param userId 用户ID
     * @param adminId 管理员ID
     */
    void deleteUser(Long userId, Long adminId);

    /**
     * 批量删除用户（逻辑删除）
     *
     * @param userIds 用户ID列表
     * @param adminId 管理员ID
     */
    void deleteUsers(List<Long> userIds, Long adminId);

    /**
     * 启用/禁用用户
     *
     * @param userId 用户ID
     * @param status 状态（0-正常，1-禁用）
     * @param adminId 管理员ID
     */
    void updateUserStatus(Long userId, Integer status, Long adminId);

    /**
     * 重置用户密码（管理员操作）
     *
     * @param userId 用户ID
     * @param newPassword 新密码
     * @param adminId 管理员ID
     */
    void resetUserPassword(Long userId, String newPassword, Long adminId);

    /**
     * 检查用户名是否可用
     *
     * @param username 用户名
     * @return 是否可用
     */
    boolean isUsernameAvailable(String username);

    /**
     * 检查邮箱是否可用
     *
     * @param email 邮箱
     * @return 是否可用
     */
    boolean isEmailAvailable(String email);

    /**
     * 检查手机号是否可用
     *
     * @param phone 手机号
     * @return 是否可用
     */
    boolean isPhoneAvailable(String phone);
} 