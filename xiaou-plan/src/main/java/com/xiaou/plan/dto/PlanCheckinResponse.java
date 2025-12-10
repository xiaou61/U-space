package com.xiaou.plan.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 计划打卡响应DTO
 * 
 * @author xiaou
 */
@Data
@Builder
public class PlanCheckinResponse {
    
    /**
     * 打卡记录ID
     */
    private Long recordId;
    
    /**
     * 计划ID
     */
    private Long planId;
    
    /**
     * 计划名称
     */
    private String planName;
    
    /**
     * 获得积分
     */
    private Integer pointsEarned;
    
    /**
     * 当前连续打卡天数
     */
    private Integer currentStreak;
    
    /**
     * 累计打卡天数
     */
    private Integer totalCheckinDays;
    
    /**
     * 打卡成功消息
     */
    private String message;
}
