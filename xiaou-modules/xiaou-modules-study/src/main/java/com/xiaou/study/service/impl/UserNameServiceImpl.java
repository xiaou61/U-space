package com.xiaou.study.service.impl;

import com.xiaou.common.domain.R;
import com.xiaou.redis.utils.RedisUtils;
import com.xiaou.study.service.UserNameService;
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
@Service("StudyUserNameService")
@Slf4j
public class UserNameServiceImpl implements UserNameService {

    private static final String USER_NAME_CACHE_PREFIX = "user:name:";
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
            
            // 3. 更新缓存（无论是真实姓名还是默认格式都缓存，避免重复查询）
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
    public Map<String, String> getUserNamesByIds(List<String> userIds) {
        Map<String, String> result = new HashMap<>();
        
        if (userIds == null || userIds.isEmpty()) {
            return result;
        }
        
        // 批量获取姓名（这里简化为逐个调用，实际可以优化为批量API）
        for (String userId : userIds) {
            String userName = getUserNameById(userId);
            result.put(userId, userName);
        }
        
        return result;
    }
} 