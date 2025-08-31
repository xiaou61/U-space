package com.xiaou.common.security;

/**
 * 通用Token服务接口
 *
 * @author xiaou
 */
public interface TokenService {

    /**
     * 将用户信息存储到Redis
     *
     * @param token 令牌
     * @param userInfo 用户信息（JSON字符串）
     * @param userType 用户类型（admin/user）
     */
    void storeUserInToken(String token, String userInfo, String userType);

    /**
     * 从Token中获取用户信息
     *
     * @param token 令牌
     * @param userType 用户类型（admin/user）
     * @return 用户信息JSON字符串
     */
    String getUserFromToken(String token, String userType);

    /**
     * 删除Token对应的用户信息
     *
     * @param token 令牌
     * @param userType 用户类型（admin/user）
     */
    void deleteToken(String token, String userType);

    /**
     * 将Token加入黑名单
     *
     * @param token 令牌
     * @param userType 用户类型（admin/user）
     */
    void addToBlacklist(String token, String userType);

    /**
     * 检查Token是否在黑名单中
     *
     * @param token 令牌
     * @param userType 用户类型（admin/user）
     * @return 是否在黑名单
     */
    boolean isTokenBlacklisted(String token, String userType);

    /**
     * 刷新Token
     *
     * @param token 当前令牌
     * @param userType 用户类型（admin/user）
     * @return 新的令牌
     */
    String refreshToken(String token, String userType);

    /**
     * 从Token中获取用户名
     *
     * @param token 令牌
     * @return 用户名
     */
    String getUsernameFromToken(String token);

    /**
     * 验证Token是否有效
     *
     * @param token 令牌
     * @param userType 用户类型（admin/user）
     * @return 是否有效
     */
    boolean validateToken(String token, String userType);

    /**
     * 从Token中获取用户ID
     *
     * @param token 令牌
     * @return 用户ID
     */
    Long getUserIdFromToken(String token);

    // ============= 兼容 system 模块的方法 =============

    /**
     * 保存管理员Token到Redis (兼容 system 模块)
     *
     * @param username 用户名
     * @param token 令牌
     * @param adminInfo 管理员信息JSON字符串
     */
    void saveToken(String username, String token, String adminInfo);

    /**
     * 从Redis中获取管理员信息 (兼容 system 模块)
     *
     * @param token 令牌
     * @return 管理员信息JSON字符串
     */
    String getAdminFromToken(String token);

    /**
     * 检查Token是否在黑名单中 (兼容 system 模块)
     *
     * @param token 令牌
     * @return 是否在黑名单
     */
    boolean isBlacklisted(String token);

    /**
     * 验证Token是否有效 (兼容 system 模块)
     *
     * @param token 令牌
     * @return 是否有效
     */
    boolean validateToken(String token);
} 