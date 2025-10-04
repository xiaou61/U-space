package com.xiaou.points.strategy.impl;

import com.xiaou.points.constant.LotteryConstants;
import com.xiaou.points.domain.LotteryPrizeConfig;
import com.xiaou.points.domain.UserLotteryLimit;
import com.xiaou.points.mapper.UserLotteryLimitMapper;
import com.xiaou.points.strategy.LotteryStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;

/**
 * 保底抽奖策略
 * 连续未中奖达到阈值后，必中三等奖以上
 * 
 * @author xiaou
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GuaranteeStrategy implements LotteryStrategy {
    
    private final UserLotteryLimitMapper userLotteryLimitMapper;
    private final Random random = new Random();
    
    @Override
    public LotteryPrizeConfig draw(Long userId, List<LotteryPrizeConfig> prizes) {
        if (prizes == null || prizes.isEmpty()) {
            throw new IllegalArgumentException("奖品列表不能为空");
        }
        
        // 获取用户连续未中奖次数
        int continuousNoWin = getContinuousNoWin(userId);
        
        // 判断是否触发保底
        if (continuousNoWin >= LotteryConstants.GUARANTEE_COUNT) {
            log.warn("用户{}连续{}次未中奖，触发保底机制", userId, continuousNoWin);
            return getGuaranteePrize(prizes);
        }
        
        // 未触发保底，使用普通抽奖
        return normalDraw(prizes, userId);
    }
    
    /**
     * 获取用户连续未中奖次数
     */
    private int getContinuousNoWin(Long userId) {
        UserLotteryLimit limit = userLotteryLimitMapper.selectByUserId(userId);
        return limit != null ? limit.getCurrentContinuousNoWin() : 0;
    }
    
    /**
     * 保底奖品（三等奖以上）
     */
    private LotteryPrizeConfig getGuaranteePrize(List<LotteryPrizeConfig> prizes) {
        // 筛选出三等奖及以上的奖品（prizeLevel <= 4）
        List<LotteryPrizeConfig> highValuePrizes = prizes.stream()
                .filter(p -> p.getPrizeLevel() <= 4)
                .toList();
        
        if (highValuePrizes.isEmpty()) {
            // 没有高价值奖品，返回四等奖
            return prizes.stream()
                    .filter(p -> p.getPrizeLevel() == 5)
                    .findFirst()
                    .orElse(prizes.get(0));
        }
        
        // 在高价值奖品中随机选择一个（权重相等）
        return highValuePrizes.get(random.nextInt(highValuePrizes.size()));
    }
    
    /**
     * 普通抽奖
     */
    private LotteryPrizeConfig normalDraw(List<LotteryPrizeConfig> prizes, Long userId) {
        BigDecimal totalProbability = prizes.stream()
                .map(LotteryPrizeConfig::getCurrentProbability)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        double randomValue = random.nextDouble() * totalProbability.doubleValue();
        
        double cumulativeProbability = 0.0;
        for (LotteryPrizeConfig prize : prizes) {
            cumulativeProbability += prize.getCurrentProbability().doubleValue();
            if (randomValue <= cumulativeProbability) {
                log.info("用户{}使用保底策略（未触发）抽中奖品：{}", userId, prize.getPrizeName());
                return prize;
            }
        }
        
        return prizes.get(prizes.size() - 1);
    }
    
    @Override
    public String getStrategyName() {
        return "GUARANTEE";
    }
}

