package com.xiaou.community.task;

import com.xiaou.community.service.CommunityHotPostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 社区热门帖子定时任务
 * 
 * @author xiaou
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommunityHotPostTask {
    
    private final CommunityHotPostService communityHotPostService;
    
    /**
     * 定时刷新热门帖子缓存
     * 每10分钟执行一次
     */
    @Scheduled(cron = "0 */10 * * * ?")
    public void refreshHotPosts() {
        try {
            log.info("定时任务：开始刷新热门帖子缓存");
            communityHotPostService.refreshHotPosts();
            log.info("定时任务：热门帖子缓存刷新完成");
        } catch (Exception e) {
            log.error("定时任务：刷新热门帖子缓存失败", e);
        }
    }
}

