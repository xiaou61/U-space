package com.xiaou.moyu.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.xiaou.common.utils.RedisUtil;
import com.xiaou.moyu.domain.HotTopicData;
import com.xiaou.moyu.domain.HotTopicResponse;
import com.xiaou.moyu.enums.HotTopicEnum;
import com.xiaou.moyu.service.HotTopicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Arrays;

/**
 * 热榜服务实现类
 *
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HotTopicServiceImpl implements HotTopicService {
    
    private final RedisUtil redisUtil;
    private final RestTemplate restTemplate;
    
    /**
     * 热榜API基础URL
     */
    @Value("${hot-topic.api.base-url:http://113.44.190.45:9996/api}")
    private String baseUrl;
    
    /**
     * Redis缓存键前缀
     */
    private static final String CACHE_KEY_PREFIX = "hot_topics:";
    
    /**
     * 缓存过期时间（秒）16分钟
     */
    private static final long CACHE_EXPIRE_TIME = 16 * 60;
    
    @Override
    public HotTopicResponse getHotTopicCategories() {
        try {
            // 先从缓存获取
            String cacheKey = CACHE_KEY_PREFIX + "categories";
            String cachedData = (String) redisUtil.get(cacheKey);
            
            if (StrUtil.isNotBlank(cachedData)) {
                return JSONUtil.toBean(cachedData, HotTopicResponse.class);
            }
            
            // 缓存不存在，调用API
            HotTopicResponse response = restTemplate.getForObject(baseUrl, HotTopicResponse.class);
            
            if (response != null) {
                // 存入缓存
                redisUtil.set(cacheKey, JSONUtil.toJsonStr(response), CACHE_EXPIRE_TIME);
            }
            
            return response;
        } catch (Exception e) {
            log.error("获取热榜分类信息失败", e);
            return null;
        }
    }
    
    @Override
    public HotTopicData getHotTopicData(String platform) {
        try {
            // 先从缓存获取
            String cacheKey = CACHE_KEY_PREFIX + "data:" + platform;
            String cachedData = (String) redisUtil.get(cacheKey);
            
            if (StrUtil.isNotBlank(cachedData)) {
                return JSONUtil.toBean(cachedData, HotTopicData.class);
            }
            
            // 缓存不存在，调用API
            String url = baseUrl + "/" + platform;
            HotTopicData data = restTemplate.getForObject(url, HotTopicData.class);
            
            if (data != null) {
                // 存入缓存
                redisUtil.set(cacheKey, JSONUtil.toJsonStr(data), CACHE_EXPIRE_TIME);
            }
            
            return data;
        } catch (Exception e) {
            log.error("获取热榜数据失败, platform: {}", platform, e);
            return null;
        }
    }
    
    @Override
    public Map<String, HotTopicData> getAllHotTopicData() {
        Map<String, HotTopicData> result = new HashMap<>();
        
        // 遍历所有平台，先从缓存获取，缓存不存在再调用API
        for (HotTopicEnum platform : HotTopicEnum.values()) {
            try {
                HotTopicData data = getHotTopicData(platform.getCode());
                if (data != null) {
                    result.put(platform.getCode(), data);
                }
            } catch (Exception e) {
                log.error("获取平台热榜数据失败: {}", platform.getCode(), e);
            }
        }
        
        return result;
    }
    
    @Override
    @Async("hotTopicExecutor")
    public void refreshHotTopicData() {
        int failedCount = 0;
        int totalCount = HotTopicEnum.values().length;
        
        try {
            // 并行刷新所有平台数据
            List<CompletableFuture<Boolean>> futures = Arrays.stream(HotTopicEnum.values())
                .map(platform -> CompletableFuture.supplyAsync(() -> {
                    try {
                        String url = baseUrl + "/" + platform.getCode();
                        HotTopicData data = restTemplate.getForObject(url, HotTopicData.class);
                        
                        if (data != null) {
                            String cacheKey = CACHE_KEY_PREFIX + "data:" + platform.getCode();
                            redisUtil.set(cacheKey, JSONUtil.toJsonStr(data), CACHE_EXPIRE_TIME);
                            return true;
                        }
                        return false;
                    } catch (Exception e) {
                        return false;
                    }
                }))
                .collect(Collectors.toList());
            
            // 等待所有任务完成并统计结果
            for (CompletableFuture<Boolean> future : futures) {
                try {
                    if (!future.join()) {
                        failedCount++;
                    }
                } catch (Exception e) {
                    failedCount++;
                }
            }
            
            int successCount = totalCount - failedCount;
            if (failedCount == 0) {
                log.info("热榜数据刷新成功（{}/{})", successCount, totalCount);
            } else {
                log.warn("热榜数据刷新部分失败（成功:{}/失败:{}/总数:{})", successCount, failedCount, totalCount);
            }
        } catch (Exception e) {
            log.error("热榜数据刷新异常: {}", e.getMessage());
        }
    }
}
