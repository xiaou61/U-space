package com.xiaou.system.service;

import com.xiaou.system.domain.SysAdmin;

/**
 * Token管理服务接口
 *
 * @author xiaou
 */
public interface TokenService {

    /**
     * 保存用户Token到Redis
     */
    void saveToken(String username, String token, SysAdmin admin);

    /**
     * 刷新Token
     */
    String refreshToken(String token);

    /**
     * 验证Token是否有效
     */
    boolean validateToken(String token);

    /**
     * 从Token中获取用户名
     */
    String getUsernameFromToken(String token);

    /**
     * 从Token中获取用户ID
     */
    Long getUserIdFromToken(String token);

    /**
     * 从Redis中获取用户信息
     */
    SysAdmin getAdminFromToken(String token);

    /**
     * 删除Token（登出）
     */
    void deleteToken(String token);

    /**
     * 将Token加入黑名单
     */
    void addToBlacklist(String token);

    /**
     * 检查Token是否在黑名单中
     */
    boolean isBlacklisted(String token);
} 