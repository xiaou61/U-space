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
     * 缓存过期时间（秒）15分钟
     */
    private static final long CACHE_EXPIRE_TIME = 15 * 60;
    
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
    
    @Override
    public void initializeHotTopicDataIfNeeded() {
        try {
            // 检查缓存是否已存在数据
            boolean hasCache = false;
            
            // 检查几个主要平台的缓存情况
            String[] checkPlatforms = {"weibo", "zhihu", "douyin", "kuaishou"};
            for (String platform : checkPlatforms) {
                String cacheKey = CACHE_KEY_PREFIX + "data:" + platform;
                if (redisUtil.hasKey(cacheKey)) {
                    hasCache = true;
                    break;
                }
            }
            
            if (hasCache) {
                log.info("热榜数据缓存已存在，跳过初始化");
                return;
            }
            
            log.info("热榜数据缓存为空，开始初始化数据");
            
            // 只有当缓存为空时才进行数据初始化
            int successCount = 0;
            int totalCount = HotTopicEnum.values().length;
            
            // 使用串行方式初始化，避免启动时大量并发请求
            for (HotTopicEnum platform : HotTopicEnum.values()) {
                try {
                    String cacheKey = CACHE_KEY_PREFIX + "data:" + platform.getCode();
                    // 再次检查单个平台缓存，防止在循环过程中其他地方设置了缓存
                    if (!redisUtil.hasKey(cacheKey)) {
                        String url = baseUrl + "/" + platform.getCode();
                        HotTopicData data = restTemplate.getForObject(url, HotTopicData.class);
                        
                        if (data != null) {
                            redisUtil.set(cacheKey, JSONUtil.toJsonStr(data), CACHE_EXPIRE_TIME);
                            successCount++;
                        }
                    } else {
                        successCount++; // 缓存已存在也算成功
                    }
                    
                    // 添加短暂延迟，避免请求过于频繁
                    Thread.sleep(100);
                    
                } catch (Exception e) {
                    log.warn("初始化平台[{}]热榜数据失败: {}", platform.getCode(), e.getMessage());
                }
            }
            
            log.info("热榜数据初始化完成（成功:{}/总数:{})", successCount, totalCount);
            
        } catch (Exception e) {
            log.error("热榜数据初始化异常: {}", e.getMessage());
        }
    }
}
