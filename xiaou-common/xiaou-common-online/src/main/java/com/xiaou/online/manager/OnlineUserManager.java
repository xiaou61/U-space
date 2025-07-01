package com.xiaou.online.manager;

import com.xiaou.redis.utils.RedisUtils;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
@Deprecated
public class OnlineUserManager {

    private static final String ONLINE_KEY_PREFIX = "online:user:";
    private static final Duration TTL = Duration.ofMinutes(5);
    private static final long TTL_REFRESH_THRESHOLD_MILLIS = 60 * 1000; // 剩余时间小于1分钟再续命

    /**
     * 用户活跃时调用（续命）
     */
    public static void markOnline(String userId) {
        String key = ONLINE_KEY_PREFIX + userId;
        long ttl = RedisUtils.getTimeToLive(key);
        if (ttl < 0 || ttl < TTL_REFRESH_THRESHOLD_MILLIS) {
            RedisUtils.setCacheObject(key, "1", TTL);
        }
    }

    /**
     * 判断用户是否在线
     */
    public static boolean isOnline(String userId) {
        return RedisUtils.hasKey(ONLINE_KEY_PREFIX + userId);
    }

    /**
     * 主动下线（退出登录时调用）
     */
    public static void markOffline(String userId) {
        RedisUtils.deleteObject(ONLINE_KEY_PREFIX + userId);
    }

    /**
     * 获取所有在线用户 ID 列表
     */
    public static List<String> getAllOnlineUserIds() {
        Collection<String> keys = RedisUtils.keys(ONLINE_KEY_PREFIX + "*");
        return keys.stream()
                .map(key -> key.substring(ONLINE_KEY_PREFIX.length()))
                .collect(Collectors.toList());
    }
}
