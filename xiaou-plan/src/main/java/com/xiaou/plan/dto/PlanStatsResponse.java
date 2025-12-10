package com.xiaou.plan.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 计划统计响应DTO
 * 
 * @author xiaou
 */
@Data
@Builder
public class PlanStatsResponse {
    
    /**
     * 进行中计划数
     */
    private Integer activePlanCount;
    
    /**
     * 累计打卡次数
     */
    private Integer totalCheckinCount;
    
    /**
     * 平均完成率
     */
    private Double avgCompletionRate;
    
    /**
     * 最长连续打卡天数
     */
    private Integer maxStreak;
    
    /**
     * 本周打卡次数
     */
    private Integer weekCheckinCount;
    
    /**
     * 本月打卡次数
     */
    private Integer monthCheckinCount;
    
    /**
     * 今日完成任务数
     */
    private Integer todayCompletedCount;
    
    /**
     * 今日待完成任务数
     */
    private Integer todayPendingCount;
}
