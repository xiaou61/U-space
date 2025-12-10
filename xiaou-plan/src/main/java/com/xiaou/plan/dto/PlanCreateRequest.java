package com.xiaou.plan.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 创建计划请求DTO
 * 
 * @author xiaou
 */
@Data
public class PlanCreateRequest {
    
    /**
     * 计划名称
     */
    private String planName;
    
    /**
     * 计划描述
     */
    private String planDesc;
    
    /**
     * 计划类型：1-刷题 2-学习 3-阅读 4-运动 5-自定义
     */
    private Integer planType;
    
    /**
     * 目标类型：1-数量 2-时长 3-次数
     */
    private Integer targetType;
    
    /**
     * 目标值
     */
    private Integer targetValue;
    
    /**
     * 目标单位（道/小时/次）
     */
    private String targetUnit;
    
    /**
     * 开始日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate startDate;
    
    /**
     * 结束日期（NULL表示长期）
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate endDate;
    
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
     * 重复类型：1-每日 2-工作日 3-周末 4-自定义
     */
    private Integer repeatType;
    
    /**
     * 自定义重复日（如：1,2,3,4,5 表示周一到周五）
     */
    private String repeatDays;
    
    /**
     * 提前提醒分钟数
     */
    private Integer remindBefore;
    
    /**
     * 截止提醒分钟数
     */
    private Integer remindDeadline;
    
    /**
     * 是否启用提醒：0-否 1-是
     */
    private Integer remindEnabled;
}
