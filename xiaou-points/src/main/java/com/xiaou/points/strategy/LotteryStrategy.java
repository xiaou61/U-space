package com.xiaou.points.strategy;

import com.xiaou.points.domain.LotteryPrizeConfig;

import java.util.List;

/**
 * 抽奖策略接口
 * 
 * @author xiaou
 */
public interface LotteryStrategy {
    
    /**
     * 执行抽奖
     * 
     * @param userId 用户ID
     * @param prizes 奖品列表
     * @return 中奖奖品
     */
    LotteryPrizeConfig draw(Long userId, List<LotteryPrizeConfig> prizes);
    
    /**
     * 获取策略名称
     * 
     * @return 策略名称
     */
    String getStrategyName();
}

