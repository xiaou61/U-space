package com.xiaou.moment.service.impl;

import com.xiaou.common.utils.RedisUtil;
import com.xiaou.moment.mapper.MomentMapper;
import com.xiaou.moment.service.MomentViewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 动态浏览统计Service实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MomentViewServiceImpl implements MomentViewService {
    
    private final RedisUtil redisUtil;
    private final MomentMapper momentMapper;
    
    /**
     * Redis Key前缀
     */
    private static final String VIEW_COUNT_KEY_PREFIX = "moment:view:";
    private static final String USER_VIEW_KEY_PREFIX = "moment:view:user:";
    
    /**
     * 用户浏览记录过期时间（5分钟）
     */
    private static final long USER_VIEW_EXPIRE_MINUTES = 5;
    
    @Override
    public void recordView(Long momentId, Long userId) {
        if (momentId == null) {
            return;
        }
        
        // 构建用户浏览记录Key
        String userViewKey = USER_VIEW_KEY_PREFIX + userId + ":" + momentId;
        
        // 检查用户是否在5分钟内已经浏览过
        if (redisUtil.hasKey(userViewKey)) {
            // 已经统计过，不重复统计
            return;
        }
        
        try {
            // 增加浏览数
            String viewCountKey = VIEW_COUNT_KEY_PREFIX + momentId;
            redisUtil.incr(viewCountKey, 1);
            
            // 记录用户浏览，5分钟过期（转换为秒）
            redisUtil.set(userViewKey, "1", USER_VIEW_EXPIRE_MINUTES * 60);
            
            log.debug("记录动态浏览: momentId={}, userId={}", momentId, userId);
        } catch (Exception e) {
            log.error("记录动态浏览失败: momentId={}, userId={}, error={}", momentId, userId, e.getMessage());
        }
    }
    
    @Override
    public Integer getViewCount(Long momentId) {
        if (momentId == null) {
            return 0;
        }
        
        try {
            String viewCountKey = VIEW_COUNT_KEY_PREFIX + momentId;
            Object count = redisUtil.get(viewCountKey);
            if (count != null) {
                return Integer.parseInt(count.toString());
            }
        } catch (Exception e) {
            log.error("获取动态浏览数失败: momentId={}, error={}", momentId, e.getMessage());
        }
        
        return 0;
    }
    
    @Override
    public void syncViewCountToDatabase() {
        log.info("开始同步浏览数到数据库...");
        
        try {
            // 获取所有浏览数Key
            Iterable<String> keys = redisUtil.getKeys(VIEW_COUNT_KEY_PREFIX + "*");
            if (keys == null) {
                log.info("没有需要同步的浏览数据");
                return;
            }
            
            int syncCount = 0;
            for (String key : keys) {
                try {
                    // 提取momentId
                    String momentIdStr = key.substring(VIEW_COUNT_KEY_PREFIX.length());
                    Long momentId = Long.parseLong(momentIdStr);
                    
                    // 获取Redis中的浏览数
                    Object countObj = redisUtil.get(key);
                    if (countObj == null) {
                        continue;
                    }
                    
                    Integer viewCount = Integer.parseInt(countObj.toString());
                    if (viewCount <= 0) {
                        continue;
                    }
                    
                    // 批量更新到数据库（使用增量更新）
                    for (int i = 0; i < viewCount; i++) {
                        momentMapper.incrementViewCount(momentId);
                    }
                    
                    // 同步成功后，清空Redis中的计数（保留Key，值设为0）
                    redisUtil.set(key, 0, 0);
                    
                    syncCount++;
                    log.debug("同步动态浏览数: momentId={}, count={}", momentId, viewCount);
                } catch (Exception e) {
                    log.error("同步单个动态浏览数失败: key={}, error={}", key, e.getMessage());
                }
            }
            
            log.info("浏览数同步完成，共同步 {} 个动态", syncCount);
        } catch (Exception e) {
            log.error("同步浏览数到数据库失败", e);
        }
    }
}

