package com.xiaou.common.security;

import com.xiaou.common.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;



/**
 * 通用Token服务实现类
 *
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final RedisUtil redisUtil;
    private final JwtTokenUtil jwtTokenUtil;

    private static final String TOKEN_PREFIX = "token:";
    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final long TOKEN_EXPIRATION_HOURS = 2; // 2小时
    private static final long BLACKLIST_EXPIRATION_HOURS = 24; // 24小时

    @Override
    public void storeUserInToken(String token, String userInfo, String userType) {
        try {
            String key = TOKEN_PREFIX + userType + ":" + token;
            redisUtil.set(key, userInfo, TOKEN_EXPIRATION_HOURS * 3600);
            log.debug("用户信息已存储到Redis，Token: {}, UserType: {}", token, userType);
        } catch (Exception e) {
            log.error("存储用户信息到Redis失败", e);
        }
    }

    @Override
    public String getUserFromToken(String token, String userType) {
        try {
            // 检查token是否在黑名单
            if (isTokenBlacklisted(token, userType)) {
                log.debug("Token在黑名单中: {}", token);
                return null;
            }

            // 验证token有效性
            if (!jwtTokenUtil.validateToken(token)) {
                log.debug("Token验证失败: {}", token);
                return null;
            }

            String key = TOKEN_PREFIX + userType + ":" + token;
            String userInfo = (String) redisUtil.get(key);
            if (userInfo != null) {
                log.debug("从Redis获取用户信息成功，UserType: {}", userType);
                return userInfo;
            }
        } catch (Exception e) {
            log.error("从Redis获取用户信息失败", e);
        }
        return null;
    }

    @Override
    public void deleteToken(String token, String userType) {
        try {
            String key = TOKEN_PREFIX + userType + ":" + token;
            redisUtil.del(key);
            log.debug("已删除Token对应的用户信息: {}, UserType: {}", token, userType);
        } catch (Exception e) {
            log.error("删除Token失败", e);
        }
    }

    @Override
    public void addToBlacklist(String token, String userType) {
        try {
            String key = BLACKLIST_PREFIX + userType + ":" + token;
            redisUtil.set(key, "1", BLACKLIST_EXPIRATION_HOURS * 3600);
            log.debug("Token已加入黑名单: {}, UserType: {}", token, userType);
        } catch (Exception e) {
            log.error("将Token加入黑名单失败", e);
        }
    }

    @Override
    public boolean isTokenBlacklisted(String token, String userType) {
        try {
            String key = BLACKLIST_PREFIX + userType + ":" + token;
            return redisUtil.hasKey(key);
        } catch (Exception e) {
            log.error("检查Token黑名单状态失败", e);
            return false;
        }
    }

    @Override
    public String refreshToken(String token, String userType) {
        try {
            // 验证当前token
            if (!jwtTokenUtil.validateToken(token)) {
                log.debug("Token无效，无法刷新: {}", token);
                return null;
            }

            // 获取用户信息
            String userInfo = getUserFromToken(token, userType);
            if (userInfo == null) {
                log.debug("无法获取用户信息，刷新失败: {}", token);
                return null;
            }

            // 获取用户信息
            String username = jwtTokenUtil.getUsernameFromToken(token);
            Long userId = jwtTokenUtil.getUserIdFromToken(token);

            // 生成新token
            String newToken = jwtTokenUtil.generateAccessToken(username, userId, userType);

            // 将旧token加入黑名单
            addToBlacklist(token, userType);
            deleteToken(token, userType);

            // 存储新token对应的用户信息
            storeUserInToken(newToken, userInfo, userType);

            log.info("Token刷新成功，用户: {}, UserType: {}", username, userType);
            return newToken;

        } catch (Exception e) {
            log.error("刷新Token失败", e);
            return null;
        }
    }

    @Override
    public String getUsernameFromToken(String token) {
        return jwtTokenUtil.getUsernameFromToken(token);
    }

    @Override
    public boolean validateToken(String token, String userType) {
        try {
            // 检查token是否在黑名单
            if (isTokenBlacklisted(token, userType)) {
                log.debug("Token在黑名单中: {}", token);
                return false;
            }

            // 验证token有效性
            if (!jwtTokenUtil.validateToken(token)) {
                log.debug("Token验证失败: {}", token);
                return false;
            }

            // 检查Redis中是否存在对应的用户信息
            String userInfo = getUserFromToken(token, userType);
            return userInfo != null;
        } catch (Exception e) {
            log.error("验证Token失败", e);
            return false;
        }
    }

    @Override
    public Long getUserIdFromToken(String token) {
        return jwtTokenUtil.getUserIdFromToken(token);
    }

    // ============= 兼容 system 模块的方法实现 =============

    @Override
    public void saveToken(String username, String token, String adminInfo) {
        // 兼容 system 模块的保存方式：以用户名为key
        storeUserInToken(token, adminInfo, "admin");
        
        // 同时保存用户名到token的映射关系（兼容旧的查找方式）
        try {
            String usernameKey = "token:admin:username:" + username;
            String tokenMappingKey = "token:admin:mapping:" + token;
            
            redisUtil.set(usernameKey, adminInfo, TOKEN_EXPIRATION_HOURS * 3600);
            redisUtil.set(tokenMappingKey, username, TOKEN_EXPIRATION_HOURS * 3600);
            
            log.debug("管理员Token保存成功: {}", username);
        } catch (Exception e) {
            log.error("保存管理员Token失败", e);
        }
    }

    @Override
    public String getAdminFromToken(String token) {
        return getUserFromToken(token, "admin");
    }

    @Override
    public boolean isBlacklisted(String token) {
        return isTokenBlacklisted(token, "admin");
    }

    @Override
    public boolean validateToken(String token) {
        return validateToken(token, "admin");
    }
} 