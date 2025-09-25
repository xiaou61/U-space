package com.xiaou.points.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 积分余额响应DTO
 * 
 * @author xiaou
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointsBalanceResponse {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 积分余额
     */
    private Integer totalPoints;
    
    /**
     * 积分对应人民币价值
     */
    private String balanceYuan;
    
    /**
     * 今日是否已打卡
     */
    private Boolean todayCheckedIn;
    
    /**
     * 连续打卡天数
     */
    private Integer continuousDays;
    
    /**
     * 今日打卡可得积分（如果已打卡则为0）
     */
    private Integer todayPoints;
    
    /**
     * 明天打卡可得积分
     */
    private Integer nextDayPoints;
}
