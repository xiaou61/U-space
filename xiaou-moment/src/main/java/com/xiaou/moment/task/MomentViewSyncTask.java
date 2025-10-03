package com.xiaou.moment.task;

import com.xiaou.moment.service.MomentViewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 动态浏览数同步定时任务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MomentViewSyncTask {
    
    private final MomentViewService momentViewService;
    
    /**
     * 每小时执行一次，同步浏览数到数据库
     * cron表达式：秒 分 时 日 月 周
     * 0 0 * * * ? 表示每小时的0分0秒执行
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void syncViewCount() {
        log.info("定时任务：开始同步动态浏览数");
        try {
            momentViewService.syncViewCountToDatabase();
            log.info("定时任务：动态浏览数同步完成");
        } catch (Exception e) {
            log.error("定时任务：动态浏览数同步失败", e);
        }
    }
}

