package com.xiaou.moyu.dto;

import lombok.Data;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * 薪资配置请求对象
 *
 * @author xiaou
 */
@Data
public class SalaryConfigRequest {
    
    /**
     * 月薪（元）
     */
    @NotNull(message = "月薪不能为空")
    @DecimalMin(value = "1000.00", message = "月薪不能低于1000元")
    @DecimalMax(value = "999999.99", message = "月薪不能超过999999.99元")
    private BigDecimal monthlySalary;
    
    /**
     * 每月工作天数
     */
    @NotNull(message = "每月工作天数不能为空")
    @Min(value = 1, message = "每月工作天数不能少于1天")
    @Max(value = 31, message = "每月工作天数不能超过31天")
    private Integer workDaysPerMonth;
    
    /**
     * 每日工作小时数
     */
    @NotNull(message = "每日工作小时数不能为空")
    @DecimalMin(value = "0.5", message = "每日工作小时数不能少于0.5小时")
    @DecimalMax(value = "24.0", message = "每日工作小时数不能超过24小时")
    private BigDecimal workHoursPerDay;
}
