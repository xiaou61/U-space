package com.xiaou.points.service;

/**
 * 抽奖应急服务接口
 * 
 * @author xiaou
 */
public interface LotteryEmergencyService {
    
    /**
     * 手动熔断
     * 
     * @param reason 熔断原因
     */
    void manualCircuitBreak(String reason);
    
    /**
     * 恢复服务
     */
    void resumeService();
    
    /**
     * 启用降级模式
     */
    void enableDegradation();
    
    /**
     * 禁用降级模式
     */
    void disableDegradation();
    
    /**
     * 检查是否处于熔断状态
     */
    boolean isCircuitBroken();
    
    /**
     * 检查是否处于降级状态
     */
    boolean isDegraded();
}

