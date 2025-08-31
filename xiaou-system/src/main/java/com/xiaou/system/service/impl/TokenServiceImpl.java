package com.xiaou.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.xiaou.common.constant.Constants;
import com.xiaou.common.utils.RedisUtil;
import com.xiaou.system.domain.SysAdmin;
import com.xiaou.system.security.JwtTokenUtil;
import com.xiaou.system.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Token管理服务实现类
 *
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {

    private final RedisUtil redisUtil;
    private final JwtTokenUtil jwtTokenUtil;

    /**
     * Token在Redis中的过期时间（比JWT略长，确保有效期内都能查到）
     */
    private static final long REDIS_TOKEN_EXPIRE_TIME = Constants.TOKEN_EXPIRE_TIME + 300; // 多5分钟

    @Override
    public void saveToken(String username, String token, SysAdmin admin) {
        try {
            String key = Constants.CACHE_TOKEN_PREFIX + username;
            
            // 保存用户信息到Redis，设置过期时间
            redisUtil.set(key, admin, REDIS_TOKEN_EXPIRE_TIME);
            
            // 保存Token到用户映射
            String tokenKey = Constants.CACHE_TOKEN_PREFIX + "mapping:" + token;
            redisUtil.set(tokenKey, username, REDIS_TOKEN_EXPIRE_TIME);
            
            log.debug("Token保存成功: {}", username);
        } catch (Exception e) {
            log.error("保存Token失败", e);
        }
    }

    @Override
    public String refreshToken(String token) {
        try {
            // 验证Token是否有效
            if (!validateToken(token)) {
                return null;
            }

            String username = jwtTokenUtil.getUsernameFromToken(token);
            Long userId = jwtTokenUtil.getUserIdFromToken(token);
            
            if (StrUtil.isBlank(username) || userId == null) {
                return null;
            }

            // 生成新的Token
            String newToken = jwtTokenUtil.generateAccessToken(username, userId);
            
            // 将旧Token加入黑名单
            addToBlacklist(token);
            
            // 获取用户信息并保存新Token
            SysAdmin admin = getAdminFromToken(token);
            if (admin != null) {
                saveToken(username, newToken, admin);
            }
            
            log.info("Token刷新成功: {}", username);
            return newToken;
        } catch (Exception e) {
            log.error("刷新Token失败", e);
            return null;
        }
    }

    @Override
    public boolean validateToken(String token) {
        try {
            // 检查Token是否在黑名单中
            if (isBlacklisted(token)) {
                return false;
            }

            String username = jwtTokenUtil.getUsernameFromToken(token);
            if (StrUtil.isBlank(username)) {
                return false;
            }

            // 验证JWT Token本身
            if (!jwtTokenUtil.validateToken(token, username)) {
                return false;
            }

            // 检查Redis中是否存在对应的用户信息
            String key = Constants.CACHE_TOKEN_PREFIX + username;
            return redisUtil.hasKey(key);
        } catch (Exception e) {
            log.error("验证Token失败", e);
            return false;
        }
    }

    @Override
    public String getUsernameFromToken(String token) {
        try {
            return jwtTokenUtil.getUsernameFromToken(token);
        } catch (Exception e) {
            log.error("从Token获取用户名失败", e);
            return null;
        }
    }

    @Override
    public Long getUserIdFromToken(String token) {
        try {
            return jwtTokenUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            log.error("从Token获取用户ID失败", e);
            return null;
        }
    }

    @Override
    public SysAdmin getAdminFromToken(String token) {
        try {
            String username = getUsernameFromToken(token);
            if (StrUtil.isBlank(username)) {
                return null;
            }

            String key = Constants.CACHE_TOKEN_PREFIX + username;
            Object admin = redisUtil.get(key);
            
            if (admin instanceof SysAdmin) {
                return (SysAdmin) admin;
            }
            return null;
        } catch (Exception e) {
            log.error("从Token获取用户信息失败", e);
            return null;
        }
    }

    @Override
    public void deleteToken(String token) {
        try {
            String username = getUsernameFromToken(token);
            if (StrUtil.isNotBlank(username)) {
                String key = Constants.CACHE_TOKEN_PREFIX + username;
                redisUtil.del(key);
                
                String tokenKey = Constants.CACHE_TOKEN_PREFIX + "mapping:" + token;
                redisUtil.del(tokenKey);
                
                log.info("Token删除成功: {}", username);
            }
        } catch (Exception e) {
            log.error("删除Token失败", e);
        }
    }

    @Override
    public void addToBlacklist(String token) {
        try {
            // 获取Token的剩余有效时间
            Long remainingTime = jwtTokenUtil.getTokenRemainingTime(token);
            if (remainingTime > 0) {
                String blacklistKey = Constants.CACHE_PREFIX + "blacklist:" + token;
                redisUtil.set(blacklistKey, "1", remainingTime);
                log.debug("Token已加入黑名单");
            }
        } catch (Exception e) {
            log.error("添加Token到黑名单失败", e);
        }
    }

    @Override
    public boolean isBlacklisted(String token) {
        try {
            String blacklistKey = Constants.CACHE_PREFIX + "blacklist:" + token;
            return redisUtil.hasKey(blacklistKey);
        } catch (Exception e) {
            log.error("检查Token黑名单失败", e);
            return false;
        }
    }
} 