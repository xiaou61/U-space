package com.xiaou.moyu.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 时薪计算器数据传输对象
 *
 * @author xiaou
 */
@Data
public class SalaryCalculatorDto {
    
    /**
     * 月薪（元）
     */
    private BigDecimal monthlySalary;
    
    /**
     * 每月工作天数
     */
    private Integer workDaysPerMonth;
    
    /**
     * 每日工作小时数
     */
    private BigDecimal workHoursPerDay;
    
    /**
     * 时薪（计算得出）
     */
    private BigDecimal hourlyRate;
    
    /**
     * 今日已工作小时数
     */
    private BigDecimal todayWorkHours;
    
    /**
     * 今日已赚金额
     */
    private BigDecimal todayEarnings;
    
    /**
     * 今日开始工作时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime todayStartTime;
    
    /**
     * 工作状态：0-未开始，1-进行中，2-已完成
     */
    private Integer workStatus;
    
    /**
     * 本周已工作小时数
     */
    private BigDecimal weekWorkHours;
    
    /**
     * 本周总收入
     */
    private BigDecimal weekEarnings;
    
    /**
     * 本月已工作小时数
     */
    private BigDecimal monthWorkHours;
    
    /**
     * 本月总收入
     */
    private BigDecimal monthEarnings;
    
    /**
     * 今日累计暂停时长（分钟）
     */
    private Integer totalPauseMinutes;
}
