package com.xiaou.moyu.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户薪资配置实体
 *
 * @author xiaou
 */
@Data
public class UserSalaryConfig {
    
    /**
     * 配置ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
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
     * 时薪（自动计算）
     */
    private BigDecimal hourlyRate;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
