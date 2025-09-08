package com.xiaou.common.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 消息通知缓存工具类
 */
@Component
@RequiredArgsConstructor
public class NotificationCacheUtil {
    
    private final RedisUtil redisUtil;
    
    private static final String UNREAD_COUNT_KEY = "notification:unread:";
    
    /**
     * 缓存未读数量
     */
    public void cacheUnreadCount(Long userId, int count) {
        redisUtil.set(UNREAD_COUNT_KEY + userId, count, 30 * 60); // 30分钟，转换为秒
    }
    
    /**
     * 获取缓存的未读数量
     */
    public Integer getCachedUnreadCount(Long userId) {
        return redisUtil.get(UNREAD_COUNT_KEY + userId, Integer.class);
    }
    
    /**
     * 清除未读数量缓存
     */
    public void clearUnreadCountCache(Long userId) {
        redisUtil.del(UNREAD_COUNT_KEY + userId);
    }
} 