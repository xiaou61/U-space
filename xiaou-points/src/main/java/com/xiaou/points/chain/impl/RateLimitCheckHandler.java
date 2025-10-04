package com.xiaou.points.chain.impl;

import com.xiaou.common.exception.BusinessException;
import com.xiaou.points.chain.RiskCheckHandler;
import com.xiaou.points.dto.lottery.LotteryContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

/**
 * 频率限制检查处理器
 * 
 * @author xiaou
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitCheckHandler extends RiskCheckHandler {
    
    private final RedissonClient redissonClient;
    
    // 用户级限制：每分钟最多10次
    private static final String USER_RATE_LIMIT_KEY = "lottery:ratelimit:user:";
    private static final long USER_RATE = 10;
    private static final long USER_INTERVAL = 60;
    
    // IP级限制：每分钟最多50次
    private static final String IP_RATE_LIMIT_KEY = "lottery:ratelimit:ip:";
    private static final long IP_RATE = 50;
    private static final long IP_INTERVAL = 60;
    
    // 全局限制：每秒最多1000次
    private static final String GLOBAL_RATE_LIMIT_KEY = "lottery:ratelimit:global";
    private static final long GLOBAL_RATE = 1000;
    private static final long GLOBAL_INTERVAL = 1;
    
    @Override
    public boolean check(Long userId, LotteryContext context) {
        // 1. 检查全局限流
        if (!checkGlobalRateLimit()) {
            log.warn("全局限流触发");
            throw new BusinessException("系统繁忙，请稍后再试");
        }
        
        // 2. 检查用户级限流
        if (!checkUserRateLimit(userId)) {
            log.warn("用户{}触发限流", userId);
            throw new BusinessException("操作过于频繁，请稍后再试");
        }
        
        // 3. 检查IP级限流（如果有IP信息）
        if (context.getIp() != null && !checkIpRateLimit(context.getIp())) {
            log.warn("IP{}触发限流", context.getIp());
            throw new BusinessException("该IP操作过于频繁，请稍后再试");
        }
        
        return checkNext(userId, context);
    }
    
    /**
     * 检查全局限流
     */
    private boolean checkGlobalRateLimit() {
        try {
            RRateLimiter rateLimiter = redissonClient.getRateLimiter(GLOBAL_RATE_LIMIT_KEY);
            
            // 如果限流器未配置，则配置它
            if (!rateLimiter.isExists()) {
                rateLimiter.trySetRate(RateType.OVERALL, GLOBAL_RATE, GLOBAL_INTERVAL, RateIntervalUnit.SECONDS);
            }
            
            // 尝试获取许可
            return rateLimiter.tryAcquire(1);
        } catch (Exception e) {
            log.error("全局限流检查失败", e);
            // 异常时允许通过，避免影响正常业务
            return true;
        }
    }
    
    /**
     * 检查用户级限流
     */
    private boolean checkUserRateLimit(Long userId) {
        try {
            String key = USER_RATE_LIMIT_KEY + userId;
            RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
            
            if (!rateLimiter.isExists()) {
                rateLimiter.trySetRate(RateType.OVERALL, USER_RATE, USER_INTERVAL, RateIntervalUnit.SECONDS);
            }
            
            return rateLimiter.tryAcquire(1);
        } catch (Exception e) {
            log.error("用户限流检查失败", e);
            return true;
        }
    }
    
    /**
     * 检查IP级限流
     */
    private boolean checkIpRateLimit(String ip) {
        try {
            String key = IP_RATE_LIMIT_KEY + ip;
            RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
            
            if (!rateLimiter.isExists()) {
                rateLimiter.trySetRate(RateType.OVERALL, IP_RATE, IP_INTERVAL, RateIntervalUnit.SECONDS);
            }
            
            return rateLimiter.tryAcquire(1);
        } catch (Exception e) {
            log.error("IP限流检查失败", e);
            return true;
        }
    }
}

