package com.xiaou.points.service.impl;

import com.xiaou.common.utils.RedisUtil;
import com.xiaou.points.service.LotteryEmergencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 抽奖应急服务实现
 * 
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LotteryEmergencyServiceImpl implements LotteryEmergencyService {
    
    private final RedisUtil redisUtil;
    
    private static final String CIRCUIT_BREAK_KEY = "lottery:emergency:circuit_break";
    private static final String DEGRADATION_KEY = "lottery:emergency:degradation";
    
    @Override
    public void manualCircuitBreak(String reason) {
        log.warn("手动触发熔断，原因：{}", reason);
        redisUtil.set(CIRCUIT_BREAK_KEY, reason, 3600); // 1小时 = 3600秒
    }
    
    @Override
    public void resumeService() {
        log.info("恢复抽奖服务");
        redisUtil.del(CIRCUIT_BREAK_KEY);
        redisUtil.del(DEGRADATION_KEY);
    }
    
    @Override
    public void enableDegradation() {
        log.warn("启用降级模式");
        redisUtil.set(DEGRADATION_KEY, true, 3600); // 1小时 = 3600秒
    }
    
    @Override
    public void disableDegradation() {
        log.info("禁用降级模式");
        redisUtil.del(DEGRADATION_KEY);
    }
    
    @Override
    public boolean isCircuitBroken() {
        return redisUtil.hasKey(CIRCUIT_BREAK_KEY);
    }
    
    @Override
    public boolean isDegraded() {
        return redisUtil.hasKey(DEGRADATION_KEY);
    }
}

