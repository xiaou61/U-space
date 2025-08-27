package com.xiaou.bbs.service.impl;

import com.xiaou.bbs.service.UserNameService;
import com.xiaou.common.domain.R;
import com.xiaou.redis.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户名称查询服务实现
 */
@Service("BbsUserNameService")
@Slf4j
public class UserNameServiceImpl implements UserNameService {

    private static final String USER_NAME_CACHE_PREFIX = "user:name:";
    private static final String USER_AVATAR_CACHE_PREFIX = "user:avatar:";
    private static final Duration CACHE_DURATION = Duration.ofMinutes(30); // 缓存30分钟

    @Autowired
    private RestTemplate restTemplate;

    @Value("${auth.service.url:http://localhost:8080}")
    private String authServiceUrl;

    @Override
    public String getUserNameById(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return "未知用户";
        }
        
        String cacheKey = USER_NAME_CACHE_PREFIX + userId;
        
        try {
            // 1. 先从缓存获取
            String cachedName = RedisUtils.getCacheObject(cacheKey);
            if (cachedName != null) {
                log.debug("从缓存获取用户 {} 的姓名: {}", userId, cachedName);
                return cachedName;
            }
            
            // 2. 缓存未命中，调用auth模块API查询
            String apiUrl = authServiceUrl + "/user/student/internal/name/" + userId;
            @SuppressWarnings("unchecked")
            R<String> response = restTemplate.getForObject(apiUrl, R.class);
            String resultName;
            
            if (response != null && response.getCode() == 200 && response.getData() != null) {
                resultName = response.getData().toString();
                log.debug("从auth模块API查询到用户 {} 的姓名: {}", userId, resultName);
            } else {
                // 如果查询不到姓名，返回默认格式
                resultName = "用户" + userId.substring(0, Math.min(8, userId.length()));
                log.debug("未从auth模块找到用户 {} 的姓名信息，使用默认格式: {}", userId, resultName);
            }
            
            // 3. 更新缓存
            try {
                RedisUtils.setCacheObject(cacheKey, resultName, CACHE_DURATION);
                log.debug("已缓存用户 {} 的姓名: {}", userId, resultName);
            } catch (Exception cacheError) {
                log.warn("缓存用户 {} 姓名失败", userId, cacheError);
            }
            
            return resultName;
            
        } catch (Exception e) {
            // API调用出错时，返回默认格式，避免影响主业务流程
            log.warn("调用auth模块API查询用户 {} 姓名时发生异常", userId, e);
            return "用户" + userId.substring(0, Math.min(8, userId.length()));
        }
    }

    @Override
    public String getUserAvatarById(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return "";
        }
        
        String cacheKey = USER_AVATAR_CACHE_PREFIX + userId;
        
        try {
            // 1. 先从缓存获取
            String cachedAvatar = RedisUtils.getCacheObject(cacheKey);
            if (cachedAvatar != null) {
                log.debug("从缓存获取用户 {} 的头像: {}", userId, cachedAvatar);
                return cachedAvatar;
            }
            
            // 2. 这里简化处理，实际可以调用具体的头像API
            // 目前返回默认头像或空字符串
            String defaultAvatar = "";
            
            // 3. 更新缓存
            try {
                RedisUtils.setCacheObject(cacheKey, defaultAvatar, CACHE_DURATION);
            } catch (Exception cacheError) {
                log.warn("缓存用户 {} 头像失败", userId, cacheError);
            }
            
            return defaultAvatar;
            
        } catch (Exception e) {
            log.warn("查询用户 {} 头像时发生异常", userId, e);
            return "";
        }
    }

    @Override
    public Map<String, UserInfo> getUserInfosByIds(List<String> userIds) {
        Map<String, UserInfo> result = new HashMap<>();
        
        if (userIds == null || userIds.isEmpty()) {
            return result;
        }
        
        // 批量获取用户信息（这里简化为逐个调用，实际可以优化为批量API）
        for (String userId : userIds) {
            String userName = getUserNameById(userId);
            String userAvatar = getUserAvatarById(userId);
            result.put(userId, new UserInfo(userName, userAvatar));
        }
        
        return result;
    }
} 