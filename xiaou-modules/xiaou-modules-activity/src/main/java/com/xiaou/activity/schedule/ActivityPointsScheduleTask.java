package com.xiaou.activity.schedule;

import com.xiaou.activity.domain.entity.Activity;
import com.xiaou.activity.mapper.ActivityMapper;
import com.xiaou.activity.service.ActivityService;
import com.xiaou.activity.utils.ActivityStatusUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 活动积分发放定时任务
 */
@Component
@Slf4j
public class ActivityPointsScheduleTask {

    @Autowired
    private ActivityMapper activityMapper;
    
    @Autowired
    private ActivityService activityService;

    /**
     * 自动处理已结束活动的积分发放
     * 每5分钟执行一次
     */
    @Scheduled(fixedDelay = 300000) // 300000毫秒 = 5分钟
    public void autoProcessFinishedActivities() {
        try {
            log.info("开始执行活动积分自动发放任务...");
            
            // 查询已结束但未发放积分的活动列表
            List<Activity> finishedActivities = activityMapper.selectFinishedActivitiesWithoutPoints();
            
            if (finishedActivities.isEmpty()) {
                log.debug("没有需要发放积分的已结束活动");
                return;
            }
            
            log.info("找到 {} 个需要发放积分的已结束活动", finishedActivities.size());
            
            int successCount = 0;
            int failCount = 0;
            
            for (Activity activity : finishedActivities) {
                try {
                    log.info("开始为活动 [{}] {} 发放积分...", activity.getId(), activity.getTitle());
                    
                    // 调用积分发放服务
                    var result = activityService.autoGrantPointsAfterActivity(activity.getId());
                    
                    if (result != null && result.getCode() == 200) {
                        successCount++;
                        log.info("活动 [{}] {} 积分发放成功: {}", 
                                activity.getId(), activity.getTitle(), result.getMsg());
                    } else {
                        failCount++;
                        log.warn("活动 [{}] {} 积分发放失败: {}", 
                                activity.getId(), activity.getTitle(), 
                                result != null ? result.getMsg() : "返回结果为空");
                    }
                    
                    // 每处理一个活动后稍微休息一下，避免数据库压力过大
                    Thread.sleep(1000);
                    
                } catch (Exception e) {
                    failCount++;
                    log.error("活动 [{}] {} 积分发放异常", 
                            activity.getId(), activity.getTitle(), e);
                }
            }
            
            log.info("活动积分自动发放任务完成，成功: {} 个，失败: {} 个", successCount, failCount);
            
        } catch (Exception e) {
            log.error("活动积分自动发放任务执行异常", e);
        }
    }

    /**
     * 手动触发积分发放任务（用于测试或紧急情况）
     */
    public void manualTriggerPointsGrant() {
        log.info("手动触发活动积分发放任务");
        autoProcessFinishedActivities();
    }

    /**
     * 自动更新活动状态
     * 每30分钟执行一次，主要用于数据展示一致性（业务逻辑不依赖此状态）
     */
    @Scheduled(fixedDelay = 1800000) // 1800000毫秒 = 30分钟
    public void autoUpdateActivityStatus() {
        try {
            log.info("开始执行活动状态自动更新任务...");
            
            // 查询所有有效活动（排除已取消和已禁用的）
            List<Activity> activities = activityMapper.selectValidActivitiesForStatusUpdate();
            
            if (activities.isEmpty()) {
                log.debug("没有需要更新状态的活动");
                return;
            }
            
            log.info("找到 {} 个活动需要检查状态更新", activities.size());
            
            int updatedCount = 0;
            
            for (Activity activity : activities) {
                try {
                    // 计算实时状态
                    Integer calculatedStatus = ActivityStatusUtil.calculateStatus(activity);
                    Integer currentStatus = activity.getStatus();
                    
                    // 如果计算出的状态与数据库中的状态不同，则更新
                    if (calculatedStatus != null && !calculatedStatus.equals(currentStatus)) {
                        int result = activityMapper.updateStatus(activity.getId(), calculatedStatus);
                        if (result > 0) {
                            updatedCount++;
                            log.debug("活动 [{}] {} 状态已更新：{} -> {}", 
                                    activity.getId(), activity.getTitle(),
                                    ActivityStatusUtil.getStatusName(currentStatus),
                                    ActivityStatusUtil.getStatusName(calculatedStatus));
                        }
                    }
                    
                } catch (Exception e) {
                    log.warn("更新活动 [{}] {} 状态时发生异常", 
                            activity.getId(), activity.getTitle(), e);
                }
            }
            
            log.info("活动状态自动更新任务完成，共更新了 {} 个活动的状态", updatedCount);
            
        } catch (Exception e) {
            log.error("活动状态自动更新任务执行异常", e);
        }
    }

    /**
     * 手动触发状态更新任务（用于测试或紧急情况）
     */
    public void manualTriggerStatusUpdate() {
        log.info("手动触发活动状态更新任务");
        autoUpdateActivityStatus();
    }
} 