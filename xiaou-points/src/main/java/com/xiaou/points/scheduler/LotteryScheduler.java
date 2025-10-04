package com.xiaou.points.scheduler;

import com.xiaou.points.service.LotteryDynamicAdjustService;
import com.xiaou.points.service.LotteryStockService;
import com.xiaou.points.mapper.LotteryPrizeConfigMapper;
import com.xiaou.points.mapper.UserLotteryLimitMapper;
import com.xiaou.points.domain.LotteryPrizeConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 抽奖定时任务调度器
 * 
 * @author xiaou
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LotteryScheduler {
    
    private final LotteryDynamicAdjustService dynamicAdjustService;
    private final LotteryStockService stockService;
    private final LotteryPrizeConfigMapper prizeConfigMapper;
    private final UserLotteryLimitMapper userLimitMapper;
    
    /**
     * 每小时自动调整概率
     * 每小时的第5分钟执行
     */
    @Scheduled(cron = "0 5 * * * ?")
    public void autoAdjustProbability() {
        log.info("开始执行定时任务：自动调整概率");
        
        try {
            dynamicAdjustService.autoAdjustAll();
            log.info("定时任务完成：自动调整概率");
        } catch (Exception e) {
            log.error("定时任务失败：自动调整概率", e);
        }
    }
    
    /**
     * 每30分钟检查熔断机制
     */
    @Scheduled(cron = "0 */30 * * * ?")
    public void checkCircuitBreaker() {
        log.info("开始执行定时任务：检查熔断机制");
        
        try {
            dynamicAdjustService.checkCircuitBreaker();
            log.info("定时任务完成：检查熔断机制");
        } catch (Exception e) {
            log.error("定时任务失败：检查熔断机制", e);
        }
    }
    
    /**
     * 每5分钟清理过期暂停状态
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void clearExpiredSuspend() {
        log.info("开始执行定时任务：清理过期暂停状态");
        
        try {
            List<LotteryPrizeConfig> prizes = prizeConfigMapper.selectSuspended();
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            
            int count = 0;
            for (LotteryPrizeConfig prize : prizes) {
                if (prize.getSuspendUntil() != null) {
                    // 判断是否已过期
                    if (now.isAfter(prize.getSuspendUntil())) {
                        prizeConfigMapper.resumePrize(prize.getId());
                        count++;
                        log.info("奖品{}暂停已过期，自动恢复", prize.getPrizeName());
                    }
                }
            }
            
            log.info("定时任务完成：清理过期暂停状态，恢复{}个奖品", count);
        } catch (Exception e) {
            log.error("定时任务失败：清理过期暂停状态", e);
        }
    }
    
    /**
     * 每天凌晨0点重置每日限制
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void resetDailyLimit() {
        log.info("开始执行定时任务：重置每日限制");
        
        try {
            // 重置所有用户的每日抽奖次数
            int userCount = userLimitMapper.resetDailyCount();
            log.info("重置{}个用户的每日抽奖次数", userCount);
            
            // 重置所有奖品的每日统计
            int prizeCount = prizeConfigMapper.resetDailyStats();
            log.info("重置{}个奖品的每日统计", prizeCount);
            
            log.info("定时任务完成：重置每日限制");
        } catch (Exception e) {
            log.error("定时任务失败：重置每日限制", e);
        }
    }
    
    /**
     * 每天凌晨1点汇总统计数据
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void summarizeStatistics() {
        log.info("开始执行定时任务：汇总统计数据");
        
        try {
            // 此处可以调用统计服务进行数据汇总
            // 生成前一天的完整统计报表
            log.info("定时任务完成：汇总统计数据");
        } catch (Exception e) {
            log.error("定时任务失败：汇总统计数据", e);
        }
    }
    
    /**
     * 每周一凌晨2点重置周统计
     */
    @Scheduled(cron = "0 0 2 ? * MON")
    public void resetWeeklyLimit() {
        log.info("开始执行定时任务：重置周统计");
        
        try {
            int count = userLimitMapper.resetWeeklyCount();
            log.info("重置{}个用户的周统计", count);
            log.info("定时任务完成：重置周统计");
        } catch (Exception e) {
            log.error("定时任务失败：重置周统计", e);
        }
    }
    
    /**
     * 每月1日凌晨3点重置月统计
     */
    @Scheduled(cron = "0 0 3 1 * ?")
    public void resetMonthlyLimit() {
        log.info("开始执行定时任务：重置月统计");
        
        try {
            int count = userLimitMapper.resetMonthlyCount();
            log.info("重置{}个用户的月统计", count);
            log.info("定时任务完成：重置月统计");
        } catch (Exception e) {
            log.error("定时任务失败：重置月统计", e);
        }
    }
    
    /**
     * 每10分钟同步Redis库存到数据库
     */
    @Scheduled(cron = "0 */10 * * * ?")
    public void syncStockToDatabase() {
        log.info("开始执行定时任务：同步库存到数据库");
        
        try {
            stockService.syncStockToDatabase();
            log.info("定时任务完成：同步库存到数据库");
        } catch (Exception e) {
            log.error("定时任务失败：同步库存到数据库", e);
        }
    }
}

