package com.xiaou.points.strategy.impl;

import com.xiaou.points.domain.LotteryPrizeConfig;
import com.xiaou.points.domain.UserLotteryLimit;
import com.xiaou.points.mapper.UserLotteryLimitMapper;
import com.xiaou.points.strategy.LotteryStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 动态权重抽奖策略
 * 根据用户连续未中奖次数，动态提升中奖概率
 * 
 * @author xiaou
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicWeightStrategy implements LotteryStrategy {
    
    private final UserLotteryLimitMapper userLotteryLimitMapper;
    private final Random random = new Random();
    
    @Override
    public LotteryPrizeConfig draw(Long userId, List<LotteryPrizeConfig> prizes) {
        if (prizes == null || prizes.isEmpty()) {
            throw new IllegalArgumentException("奖品列表不能为空");
        }
        
        // 获取用户连续未中奖次数
        int continuousNoWin = getContinuousNoWin(userId);
        
        // 根据连续未中奖次数调整概率
        List<LotteryPrizeConfig> adjustedPrizes = adjustPrizesByUserBehavior(prizes, continuousNoWin);
        
        // 执行加权随机抽奖
        return weightedRandomDraw(adjustedPrizes, userId);
    }
    
    /**
     * 获取用户连续未中奖次数
     */
    private int getContinuousNoWin(Long userId) {
        UserLotteryLimit limit = userLotteryLimitMapper.selectByUserId(userId);
        return limit != null ? limit.getCurrentContinuousNoWin() : 0;
    }
    
    /**
     * 根据用户行为调整奖品概率
     * 连续未中奖越多，高价值奖品概率提升越多
     */
    private List<LotteryPrizeConfig> adjustPrizesByUserBehavior(List<LotteryPrizeConfig> prizes, int continuousNoWin) {
        if (continuousNoWin < 5) {
            // 未达到调整阈值，使用原概率
            return prizes;
        }
        
        List<LotteryPrizeConfig> adjustedPrizes = new ArrayList<>();
        
        // 计算权重系数（连续未中奖次数越多，系数越大）
        double weightFactor = 1.0 + (continuousNoWin - 5) * 0.05; // 每5次未中奖，提升5%
        weightFactor = Math.min(weightFactor, 1.5); // 最多提升50%
        
        for (LotteryPrizeConfig prize : prizes) {
            LotteryPrizeConfig adjusted = new LotteryPrizeConfig();
            // 复制原对象属性（这里简化处理，实际应用BeanUtils）
            adjusted.setId(prize.getId());
            adjusted.setPrizeName(prize.getPrizeName());
            adjusted.setPrizeLevel(prize.getPrizeLevel());
            adjusted.setPrizePoints(prize.getPrizePoints());
            
            // 调整概率：未中奖概率降低，其他奖品概率提升
            if (prize.getPrizeLevel() == 8) {
                // 未中奖概率降低
                BigDecimal newProb = prize.getCurrentProbability()
                        .divide(BigDecimal.valueOf(weightFactor), 8, RoundingMode.HALF_UP);
                adjusted.setCurrentProbability(newProb);
            } else {
                // 其他奖品概率提升
                BigDecimal newProb = prize.getCurrentProbability()
                        .multiply(BigDecimal.valueOf(weightFactor));
                adjusted.setCurrentProbability(newProb);
            }
            
            adjustedPrizes.add(adjusted);
        }
        
        // 归一化概率
        normalizeProbabilities(adjustedPrizes);
        
        log.info("用户连续{}次未中奖，使用动态权重策略，权重系数：{}", continuousNoWin, weightFactor);
        return adjustedPrizes;
    }
    
    /**
     * 归一化概率，确保总和为1
     */
    private void normalizeProbabilities(List<LotteryPrizeConfig> prizes) {
        BigDecimal total = prizes.stream()
                .map(LotteryPrizeConfig::getCurrentProbability)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        for (LotteryPrizeConfig prize : prizes) {
            BigDecimal normalized = prize.getCurrentProbability()
                    .divide(total, 8, RoundingMode.HALF_UP);
            prize.setCurrentProbability(normalized);
        }
    }
    
    /**
     * 加权随机抽奖
     */
    private LotteryPrizeConfig weightedRandomDraw(List<LotteryPrizeConfig> prizes, Long userId) {
        BigDecimal totalProbability = prizes.stream()
                .map(LotteryPrizeConfig::getCurrentProbability)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        double randomValue = random.nextDouble() * totalProbability.doubleValue();
        
        double cumulativeProbability = 0.0;
        for (LotteryPrizeConfig prize : prizes) {
            cumulativeProbability += prize.getCurrentProbability().doubleValue();
            if (randomValue <= cumulativeProbability) {
                log.info("用户{}使用动态权重策略抽中奖品：{}", userId, prize.getPrizeName());
                return prize;
            }
        }
        
        return prizes.get(prizes.size() - 1);
    }
    
    @Override
    public String getStrategyName() {
        return "DYNAMIC_WEIGHT";
    }
}

