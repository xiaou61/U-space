package com.xiaou.plan.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalTime;

/**
 * 今日任务响应DTO
 * 
 * @author xiaou
 */
@Data
@Builder
public class TodayTaskResponse {
    
    /**
     * 计划ID
     */
    private Long planId;
    
    /**
     * 计划名称
     */
    private String planName;
    
    /**
     * 计划类型
     */
    private Integer planType;
    
    /**
     * 计划类型描述
     */
    private String planTypeDesc;
    
    /**
     * 计划类型图标
     */
    private String planTypeIcon;
    
    /**
     * 目标值
     */
    private Integer targetValue;
    
    /**
     * 目标单位
     */
    private String targetUnit;
    
    /**
     * 每日开始时间
     */
    @JsonFormat(pattern = "HH:mm", timezone = "GMT+8")
    private LocalTime dailyStartTime;
    
    /**
     * 每日截止时间
     */
    @JsonFormat(pattern = "HH:mm", timezone = "GMT+8")
    private LocalTime dailyEndTime;
    
    /**
     * 任务状态：0-待完成 1-已完成 2-已过期
     */
    private Integer taskStatus;
    
    /**
     * 任务状态描述
     */
    private String taskStatusDesc;
    
    /**
     * 是否已打卡
     */
    private Boolean checkedIn;
    
    /**
     * 剩余时间（分钟）
     */
    private Long remainingMinutes;
    
    /**
     * 剩余时间描述
     */
    private String remainingTimeDesc;
    
    /**
     * 当前连续打卡天数
     */
    private Integer currentStreak;
}
