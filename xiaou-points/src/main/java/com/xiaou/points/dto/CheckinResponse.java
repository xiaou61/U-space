package com.xiaou.points.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户打卡响应DTO
 * 
 * @author xiaou
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckinResponse {
    
    /**
     * 本次获得积分
     */
    private Integer pointsEarned;
    
    /**
     * 连续打卡天数
     */
    private Integer continuousDays;
    
    /**
     * 明天打卡可得积分
     */
    private Integer nextDayPoints;
    
    /**
     * 当前积分余额
     */
    private Integer totalBalance;
    
    /**
     * 积分对应人民币价值
     */
    private String balanceYuan;
    
    /**
     * 打卡描述
     */
    private String description;
    
    /**
     * 是否为周奖励日
     */
    private Boolean isWeekBonusDay;
}
