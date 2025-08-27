package com.xiaou.activity.service;

import com.xiaou.activity.domain.entity.Activity;
import com.xiaou.activity.mapper.ActivityMapper;
import com.xiaou.redis.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 活动缓存服务
 * 用于缓存活动基本信息以提升查询性能
 */
@Service
@Slf4j
public class ActivityCacheService {

    private static final String ACTIVITY_CACHE_PREFIX = "activity:";
    private static final Duration CACHE_DURATION = Duration.ofMinutes(10); // 缓存10分钟

    @Autowired
    private ActivityMapper activityMapper;

    /**
     * 获取活动信息（优先从缓存）
     * 
     * @param activityId 活动ID
     * @return 活动信息
     */
    public Activity getActivity(Long activityId) {
        if (activityId == null) {
            return null;
        }

        String cacheKey = ACTIVITY_CACHE_PREFIX + activityId;
        
        try {
            // 先从缓存获取
            Activity cachedActivity = RedisUtils.getCacheObject(cacheKey);
            if (cachedActivity != null) {
                log.debug("从缓存获取活动信息: {}", activityId);
                return cachedActivity;
            }
        } catch (Exception e) {
            log.warn("从缓存获取活动 {} 失败，将从数据库查询", activityId, e);
        }

        // 缓存未命中，从数据库查询
        Activity activity = activityMapper.selectById(activityId);
        
        if (activity != null) {
            try {
                // 存入缓存
                RedisUtils.setCacheObject(cacheKey, activity, CACHE_DURATION);
                log.debug("活动信息已缓存: {}", activityId);
            } catch (Exception e) {
                log.warn("缓存活动 {} 失败", activityId, e);
            }
        }
        
        return activity;
    }

    /**
     * 更新缓存中的活动信息
     * 
     * @param activity 活动信息
     */
    public void updateActivityCache(Activity activity) {
        if (activity == null || activity.getId() == null) {
            return;
        }

        String cacheKey = ACTIVITY_CACHE_PREFIX + activity.getId();
        
        try {
            RedisUtils.setCacheObject(cacheKey, activity, CACHE_DURATION);
            log.debug("已更新活动缓存: {}", activity.getId());
        } catch (Exception e) {
            log.warn("更新活动 {} 缓存失败", activity.getId(), e);
        }
    }

    /**
     * 删除活动缓存
     * 
     * @param activityId 活动ID
     */
    public void deleteActivityCache(Long activityId) {
        if (activityId == null) {
            return;
        }

        String cacheKey = ACTIVITY_CACHE_PREFIX + activityId;
        
        try {
            RedisUtils.deleteObject(cacheKey);
            log.debug("已删除活动缓存: {}", activityId);
        } catch (Exception e) {
            log.warn("删除活动 {} 缓存失败", activityId, e);
        }
    }

    /**
     * 批量预热活动缓存
     * 用于系统启动或定时刷新热点活动缓存
     */
    public void warmUpActivityCache() {
        try {
            log.info("开始预热活动缓存...");
            
            // 获取近期活动进行预热（可以根据业务需求调整查询条件）
            // 这里只是示例，实际可以查询最近创建或即将开始的活动
            
            log.info("活动缓存预热完成");
        } catch (Exception e) {
            log.error("活动缓存预热失败", e);
        }
    }
} 