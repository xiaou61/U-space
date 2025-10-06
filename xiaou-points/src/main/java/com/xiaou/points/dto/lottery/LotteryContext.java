package com.xiaou.points.dto.lottery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 抽奖上下文
 * 
 * @author xiaou
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotteryContext {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * IP地址
     */
    private String ip;
    
    /**
     * 设备信息
     */
    private String device;
    
    /**
     * 抽奖策略
     */
    private String strategy;
    
    /**
     * 抽奖策略类型
     */
    private String strategyType;
    
    /**
     * 奖品列表
     */
    private java.util.List<com.xiaou.points.domain.LotteryPrizeConfig> prizes;
    
    /**
     * 用户限制信息
     */
    private com.xiaou.points.domain.UserLotteryLimit userLimit;
    
    /**
     * 用户积分余额
     */
    private com.xiaou.points.domain.UserPointsBalance userBalance;
}

