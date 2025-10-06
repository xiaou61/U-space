package com.xiaou.points.service;

/**
 * 抽奖动态调整服务接口
 * 
 * @author xiaou
 */
public interface LotteryDynamicAdjustService {
    
    /**
     * 自动调整所有奖品概率
     */
    void autoAdjustAll();
    
    /**
     * 自动调整单个奖品概率
     */
    void autoAdjustPrize(Long prizeId);
    
    /**
     * 检查并触发熔断机制
     */
    void checkCircuitBreaker();
}

