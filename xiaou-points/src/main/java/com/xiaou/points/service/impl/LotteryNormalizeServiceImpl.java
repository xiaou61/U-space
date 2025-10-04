package com.xiaou.points.service.impl;

import com.xiaou.points.domain.LotteryPrizeConfig;
import com.xiaou.points.mapper.LotteryPrizeConfigMapper;
import com.xiaou.points.service.LotteryNormalizeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 概率归一化服务实现
 * 
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LotteryNormalizeServiceImpl implements LotteryNormalizeService {
    
    private final LotteryPrizeConfigMapper prizeConfigMapper;
    
    /**
     * 误差阈值：0.0001
     */
    private static final BigDecimal ERROR_THRESHOLD = new BigDecimal("0.0001");
    
    /**
     * 目标概率总和：1.0
     */
    private static final BigDecimal TARGET_SUM = BigDecimal.ONE;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal normalizeAllProbabilities() {
        // 获取所有启用的奖品
        List<LotteryPrizeConfig> prizes = prizeConfigMapper.selectAllActive();
        
        if (prizes.isEmpty()) {
            log.warn("没有启用的奖品，无需归一化");
            return BigDecimal.ZERO;
        }
        
        return normalizePrizeList(prizes);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BigDecimal normalizeProbabilities(List<Long> prizeIds) {
        if (prizeIds == null || prizeIds.isEmpty()) {
            log.warn("奖品ID列表为空，无需归一化");
            return BigDecimal.ZERO;
        }
        
        // 查询指定的奖品
        List<LotteryPrizeConfig> prizes = prizeIds.stream()
                .map(prizeConfigMapper::selectById)
                .filter(prize -> prize != null && prize.getIsActive() == 1)
                .collect(Collectors.toList());
        
        if (prizes.isEmpty()) {
            log.warn("没有有效的奖品，无需归一化");
            return BigDecimal.ZERO;
        }
        
        return normalizePrizeList(prizes);
    }
    
    @Override
    public boolean needsNormalization() {
        BigDecimal sum = validateProbabilitySum();
        
        // 计算与目标值的差异
        BigDecimal diff = sum.subtract(TARGET_SUM).abs();
        
        // 如果差异超过阈值，则需要归一化
        boolean needsNorm = diff.compareTo(ERROR_THRESHOLD) > 0;
        
        if (needsNorm) {
            log.warn("概率总和{}偏离目标1.0，差异：{}，需要归一化", sum, diff);
        }
        
        return needsNorm;
    }
    
    @Override
    public BigDecimal validateProbabilitySum() {
        List<LotteryPrizeConfig> prizes = prizeConfigMapper.selectAllActive();
        
        if (prizes.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal sum = prizes.stream()
                .map(LotteryPrizeConfig::getCurrentProbability)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        log.debug("当前概率总和：{}", sum);
        
        return sum;
    }
    
    /**
     * 归一化奖品列表
     */
    private BigDecimal normalizePrizeList(List<LotteryPrizeConfig> prizes) {
        // 1. 计算当前概率总和
        BigDecimal currentSum = prizes.stream()
                .map(LotteryPrizeConfig::getCurrentProbability)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        log.info("归一化前概率总和：{}", currentSum);
        
        if (currentSum.compareTo(BigDecimal.ZERO) == 0) {
            log.error("概率总和为0，无法归一化");
            return BigDecimal.ZERO;
        }
        
        // 2. 计算归一化因子
        BigDecimal normalizeFactor = TARGET_SUM.divide(currentSum, 10, RoundingMode.HALF_UP);
        
        log.info("归一化因子：{}", normalizeFactor);
        
        // 3. 归一化每个奖品的概率
        BigDecimal normalizedSum = BigDecimal.ZERO;
        
        for (int i = 0; i < prizes.size(); i++) {
            LotteryPrizeConfig prize = prizes.get(i);
            BigDecimal oldProbability = prize.getCurrentProbability();
            BigDecimal newProbability;
            
            if (i == prizes.size() - 1) {
                // 最后一个奖品：用 1.0 - 前面所有概率之和，确保精确等于1.0
                newProbability = TARGET_SUM.subtract(normalizedSum);
            } else {
                // 其他奖品：乘以归一化因子
                newProbability = oldProbability.multiply(normalizeFactor)
                        .setScale(8, RoundingMode.HALF_UP);
                normalizedSum = normalizedSum.add(newProbability);
            }
            
            // 更新概率
            prizeConfigMapper.updateCurrentProbability(prize.getId(), newProbability);
            
            log.debug("奖品{} 概率调整：{} -> {}", 
                    prize.getPrizeName(), oldProbability, newProbability);
        }
        
        // 4. 验证归一化结果
        BigDecimal finalSum = validateProbabilitySum();
        BigDecimal diff = finalSum.subtract(TARGET_SUM).abs();
        
        if (diff.compareTo(ERROR_THRESHOLD) > 0) {
            log.warn("归一化后概率总和{}仍有偏差，差异：{}", finalSum, diff);
        } else {
            log.info("归一化成功，概率总和：{}，误差：{}", finalSum, diff);
        }
        
        return finalSum;
    }
}

