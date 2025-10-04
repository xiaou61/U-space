package com.xiaou.points.chain.impl;

import com.xiaou.common.exception.BusinessException;
import com.xiaou.points.chain.RiskCheckHandler;
import com.xiaou.points.dto.lottery.LotteryContext;
import com.xiaou.points.mapper.UserLotteryLimitMapper;
import com.xiaou.points.domain.UserLotteryLimit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 冷却时间检查处理器
 * 
 * @author xiaou
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CooldownCheckHandler extends RiskCheckHandler {
    
    private final UserLotteryLimitMapper userLimitMapper;
    
    /**
     * 普通用户冷却时间：3秒
     */
    private static final long NORMAL_COOLDOWN_SECONDS = 3;
    
    /**
     * 高风险用户冷却时间：10秒
     */
    private static final long HIGH_RISK_COOLDOWN_SECONDS = 10;
    
    /**
     * 连续抽奖阈值：10次
     */
    private static final int CONTINUOUS_DRAW_THRESHOLD = 10;
    
    /**
     * 连续抽奖后冷却时间：60秒
     */
    private static final long CONTINUOUS_COOLDOWN_SECONDS = 60;
    
    @Override
    public boolean check(Long userId, LotteryContext context) {
        UserLotteryLimit limit = userLimitMapper.selectByUserId(userId);
        
        if (limit == null || limit.getLastDrawTime() == null) {
            // 首次抽奖，无需检查
            return checkNext(userId, context);
        }
        
        LocalDateTime lastDrawTime = limit.getLastDrawTime();
        LocalDateTime now = LocalDateTime.now();
        
        long secondsSinceLastDraw = ChronoUnit.SECONDS.between(lastDrawTime, now);
        
        // 1. 检查是否是高风险用户
        if (limit.getRiskLevel() != null && limit.getRiskLevel() >= 2) {
            if (secondsSinceLastDraw < HIGH_RISK_COOLDOWN_SECONDS) {
                long remaining = HIGH_RISK_COOLDOWN_SECONDS - secondsSinceLastDraw;
                throw new BusinessException("高风险用户需要等待" + remaining + "秒后才能继续抽奖");
            }
        }
        
        // 2. 检查是否频繁抽奖（每分钟超过10次）
        if (limit.getTodayDrawCount() != null && limit.getTodayDrawCount() > 0) {
            // 简单判断：如果今日抽奖次数是10的倍数，且距离上次抽奖不到10秒
            if (limit.getTodayDrawCount() % CONTINUOUS_DRAW_THRESHOLD == 0 && 
                secondsSinceLastDraw < CONTINUOUS_COOLDOWN_SECONDS) {
                long remaining = CONTINUOUS_COOLDOWN_SECONDS - secondsSinceLastDraw;
                throw new BusinessException("连续抽奖过多，需要冷却" + remaining + "秒");
            }
        }
        
        // 3. 普通冷却检查
        if (secondsSinceLastDraw < NORMAL_COOLDOWN_SECONDS) {
            long remaining = NORMAL_COOLDOWN_SECONDS - secondsSinceLastDraw;
            throw new BusinessException("操作过快，请等待" + remaining + "秒");
        }
        
        return checkNext(userId, context);
    }
}

