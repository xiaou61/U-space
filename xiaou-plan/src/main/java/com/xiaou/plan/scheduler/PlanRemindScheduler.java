package com.xiaou.plan.scheduler;

import com.xiaou.common.domain.Notification;
import com.xiaou.common.enums.NotificationStatusEnum;
import com.xiaou.common.service.NotificationService;
import com.xiaou.plan.domain.PlanRemindTask;
import com.xiaou.plan.domain.UserPlan;
import com.xiaou.plan.mapper.PlanRemindTaskMapper;
import com.xiaou.plan.mapper.UserPlanMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 计划提醒调度器
 * 
 * @author xiaou
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlanRemindScheduler {
    
    private final UserPlanMapper planMapper;
    private final PlanRemindTaskMapper remindTaskMapper;
    private final NotificationService notificationService;
    
    /**
     * 每天凌晨生成当日的提醒任务
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void generateDailyRemindTasks() {
        log.info("开始生成当日提醒任务");
        LocalDate today = LocalDate.now();
        
        try {
            // 查询所有需要提醒的计划
            List<UserPlan> plans = planMapper.selectPlansForRemind(today);
            List<PlanRemindTask> tasksToInsert = new ArrayList<>();
            
            for (UserPlan plan : plans) {
                // 检查今天是否已生成提醒任务
                int existCount = remindTaskMapper.countByPlanIdAndDate(plan.getId(), today);
                if (existCount > 0) {
                    continue;
                }
                
                LocalDateTime now = LocalDateTime.now();
                
                // 生成开始提醒任务
                if (plan.getDailyStartTime() != null && plan.getRemindBefore() != null && plan.getRemindBefore() > 0) {
                    LocalDateTime startRemindTime = LocalDateTime.of(today, plan.getDailyStartTime())
                            .minusMinutes(plan.getRemindBefore());
                    
                    if (startRemindTime.isAfter(now)) {
                        PlanRemindTask startTask = new PlanRemindTask();
                        startTask.setPlanId(plan.getId());
                        startTask.setUserId(plan.getUserId());
                        startTask.setRemindType(1); // 开始提醒
                        startTask.setRemindDate(today);
                        startTask.setRemindTime(startRemindTime);
                        startTask.setStatus(0); // 待发送
                        startTask.setCreateTime(now);
                        tasksToInsert.add(startTask);
                    }
                }
                
                // 生成截止提醒任务
                if (plan.getDailyEndTime() != null && plan.getRemindDeadline() != null && plan.getRemindDeadline() > 0) {
                    LocalDateTime deadlineRemindTime = LocalDateTime.of(today, plan.getDailyEndTime())
                            .minusMinutes(plan.getRemindDeadline());
                    
                    if (deadlineRemindTime.isAfter(now)) {
                        PlanRemindTask deadlineTask = new PlanRemindTask();
                        deadlineTask.setPlanId(plan.getId());
                        deadlineTask.setUserId(plan.getUserId());
                        deadlineTask.setRemindType(2); // 截止提醒
                        deadlineTask.setRemindDate(today);
                        deadlineTask.setRemindTime(deadlineRemindTime);
                        deadlineTask.setStatus(0); // 待发送
                        deadlineTask.setCreateTime(now);
                        tasksToInsert.add(deadlineTask);
                    }
                }
            }
            
            // 批量插入提醒任务
            if (!tasksToInsert.isEmpty()) {
                remindTaskMapper.batchInsert(tasksToInsert);
                log.info("生成当日提醒任务完成，共{}个任务", tasksToInsert.size());
            }
            
        } catch (Exception e) {
            log.error("生成当日提醒任务失败", e);
        }
    }
    
    /**
     * 每分钟扫描待发送的提醒任务
     */
    @Scheduled(cron = "0 * * * * ?")
    public void scanAndSendRemindTasks() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMinuteLater = now.plusMinutes(1);
        
        try {
            // 查询即将到期的提醒任务
            List<PlanRemindTask> tasks = remindTaskMapper.selectPendingTasks(now, oneMinuteLater);
            
            for (PlanRemindTask task : tasks) {
                sendRemind(task);
            }
            
        } catch (Exception e) {
            log.error("扫描提醒任务失败", e);
        }
    }
    
    /**
     * 发送提醒通知
     */
    private void sendRemind(PlanRemindTask task) {
        try {
            // 获取计划信息
            UserPlan plan = planMapper.selectById(task.getPlanId());
            if (plan == null) {
                remindTaskMapper.updateStatus(task.getId(), 2, LocalDateTime.now()); // 取消
                return;
            }
            
            // 构建通知内容
            String title;
            String content;
            if (task.getRemindType() == 1) {
                // 开始提醒
                title = "📋 " + plan.getPlanName() + " 即将开始";
                content = "您的任务「" + plan.getPlanName() + "」将在 " + 
                        formatTime(plan.getDailyStartTime()) + " 开始，目标：" + 
                        plan.getTargetValue() + plan.getTargetUnit() + "。加油！";
            } else {
                // 截止提醒
                title = "⏰ " + plan.getPlanName() + " 即将截止";
                content = "您的任务「" + plan.getPlanName() + "」将在 " + 
                        formatTime(plan.getDailyEndTime()) + " 截止，还未完成哦~";
            }
            
            // 创建通知
            Notification notification = new Notification();
            notification.setTitle(title);
            notification.setContent(content);
            notification.setType("SYSTEM");
            notification.setPriority("NORMAL");
            notification.setReceiverId(task.getUserId());
            notification.setSourceModule("plan");
            notification.setSourceId(String.valueOf(plan.getId()));
            notification.setStatus(NotificationStatusEnum.UNREAD.getCode());
            notification.setCreatedTime(LocalDateTime.now());
            
            // 发送通知
            notificationService.sendNotification(notification);
            
            // 更新任务状态
            remindTaskMapper.updateStatus(task.getId(), 1, LocalDateTime.now());
            
            log.info("发送计划提醒成功，任务ID：{}，用户ID：{}", task.getId(), task.getUserId());
            
        } catch (Exception e) {
            log.error("发送提醒失败，任务ID：{}", task.getId(), e);
        }
    }
    
    /**
     * 格式化时间
     */
    private String formatTime(LocalTime time) {
        if (time == null) {
            return "";
        }
        return String.format("%02d:%02d", time.getHour(), time.getMinute());
    }
    
    /**
     * 每周清理历史提醒任务
     */
    @Scheduled(cron = "0 0 3 ? * MON")
    public void cleanOldRemindTasks() {
        try {
            LocalDate beforeDate = LocalDate.now().minusDays(7);
            int deleted = remindTaskMapper.deleteOldTasks(beforeDate);
            log.info("清理历史提醒任务完成，删除{}条记录", deleted);
        } catch (Exception e) {
            log.error("清理历史提醒任务失败", e);
        }
    }
}
