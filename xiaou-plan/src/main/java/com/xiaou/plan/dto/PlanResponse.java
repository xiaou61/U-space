package com.xiaou.plan.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 计划响应DTO
 * 
 * @author xiaou
 */
@Data
@Builder
public class PlanResponse {
    
    private Long id;
    private Long userId;
    private String planName;
    private String planDesc;
    private Integer planType;
    private String planTypeDesc;
    private String planTypeIcon;
    private Integer targetType;
    private Integer targetValue;
    private String targetUnit;
    
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate startDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate endDate;
    
    @JsonFormat(pattern = "HH:mm", timezone = "GMT+8")
    private LocalTime dailyStartTime;
    
    @JsonFormat(pattern = "HH:mm", timezone = "GMT+8")
    private LocalTime dailyEndTime;
    
    private Integer repeatType;
    private String repeatTypeDesc;
    private String repeatDays;
    private Integer remindBefore;
    private Integer remindDeadline;
    private Integer remindEnabled;
    private Integer status;
    private String statusDesc;
    private Integer totalCheckinDays;
    private Integer currentStreak;
    private Integer maxStreak;
    
    /**
     * 完成率（百分比）
     */
    private Double completionRate;
    
    /**
     * 今日是否已打卡
     */
    private Boolean todayCheckedIn;
    
    /**
     * 今日是否需要打卡
     */
    private Boolean todayNeedCheckin;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
