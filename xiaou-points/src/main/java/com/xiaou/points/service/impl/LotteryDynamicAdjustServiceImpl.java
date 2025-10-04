package com.xiaou.points.service.impl;

import com.xiaou.points.domain.LotteryAdjustHistory;
import com.xiaou.points.domain.LotteryPrizeConfig;
import com.xiaou.points.enums.AdjustStrategyEnum;
import com.xiaou.points.mapper.LotteryAdjustHistoryMapper;
import com.xiaou.points.mapper.LotteryPrizeConfigMapper;
import com.xiaou.points.service.LotteryDynamicAdjustService;
import com.xiaou.points.service.LotteryNormalizeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 抽奖动态调整服务实现
 * 
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LotteryDynamicAdjustServiceImpl implements LotteryDynamicAdjustService {
    
    private final LotteryPrizeConfigMapper prizeConfigMapper;
    private final LotteryAdjustHistoryMapper adjustHistoryMapper;
    private final LotteryNormalizeService normalizeService;
    
    /**
     * 调整因子
     */
    private static final BigDecimal INCREASE_FACTOR = BigDecimal.valueOf(1.05);
    private static final BigDecimal DECREASE_FACTOR = BigDecimal.valueOf(0.95);
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoAdjustAll() {
        log.info("开始自动调整所有奖品概率");
        
        List<LotteryPrizeConfig> prizes = prizeConfigMapper.selectAllActive();
        int adjustCount = 0;
        for (LotteryPrizeConfig prize : prizes) {
            if (AdjustStrategyEnum.AUTO.getCode().equals(prize.getAdjustStrategy())) {
                autoAdjustPrize(prize.getId());
                adjustCount++;
            }
        }
        
        // 调整后进行概率归一化，确保所有概率之和为1.0
        if (adjustCount > 0) {
            log.info("自动调整完成，共调整{}个奖品，开始归一化概率", adjustCount);
            normalizeService.normalizeAllProbabilities();
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoAdjustPrize(Long prizeId) {
        LotteryPrizeConfig prize = prizeConfigMapper.selectById(prizeId);
        if (prize == null || !AdjustStrategyEnum.AUTO.getCode().equals(prize.getAdjustStrategy())) {
            return;
        }
        
        // 计算实际回报率
        BigDecimal actualRate = calculateActualReturnRate(prize);
        BigDecimal targetRate = prize.getTargetReturnRate();
        BigDecimal maxRate = prize.getMaxReturnRate();
        BigDecimal minRate = prize.getMinReturnRate();
        
        BigDecimal oldProbability = prize.getCurrentProbability();
        BigDecimal newProbability = oldProbability;
        String reason = null;
        
        // 判断是否需要调整
        if (actualRate.compareTo(maxRate) > 0) {
            // 实际回报率过高，降低概率
            newProbability = oldProbability.multiply(DECREASE_FACTOR);
            reason = "实际回报率" + actualRate + "超过最大阈值" + maxRate + "，降低概率";
            log.warn("奖品{}回报率过高，从{}降低到{}", prizeId, oldProbability, newProbability);
        } else if (actualRate.compareTo(minRate) < 0 && prize.getTotalDrawCount() > 100) {
            // 实际回报率过低，提高概率（需要有足够的样本数）
            newProbability = oldProbability.multiply(INCREASE_FACTOR);
            reason = "实际回报率" + actualRate + "低于最小阈值" + minRate + "，提高概率";
            log.info("奖品{}回报率过低，从{}提高到{}", prizeId, oldProbability, newProbability);
        }
        
        // 如果需要调整
        if (!newProbability.equals(oldProbability)) {
            // 更新概率
            prizeConfigMapper.updateCurrentProbability(prizeId, newProbability);
            
            // 记录调整历史
            LotteryAdjustHistory history = new LotteryAdjustHistory();
            history.setPrizeId(prizeId);
            history.setAdjustType("AUTO");
            history.setOldProbability(oldProbability);
            history.setNewProbability(newProbability);
            history.setOldReturnRate(actualRate);
            history.setNewReturnRate(actualRate);
            history.setAdjustReason(reason);
            history.setOperator("SYSTEM");
            history.setOperatorId(0L); // 0表示系统自动
            history.setCreateTime(LocalDateTime.now());
            adjustHistoryMapper.insert(history);
            
            log.info("奖品{}概率调整完成：{} -> {}，将在批量调整后进行归一化", prizeId, oldProbability, newProbability);
        }
    }
    
    @Override
    public void checkCircuitBreaker() {
        log.info("检查熔断机制");
        
        List<LotteryPrizeConfig> prizes = prizeConfigMapper.selectAllActive();
        for (LotteryPrizeConfig prize : prizes) {
            BigDecimal actualRate = calculateActualReturnRate(prize);
            BigDecimal maxRate = prize.getMaxReturnRate();
            
            // 如果回报率超过最大阈值的1.5倍，触发熔断
            BigDecimal breakerThreshold = maxRate.multiply(BigDecimal.valueOf(1.5));
            if (actualRate.compareTo(breakerThreshold) > 0) {
                log.error("奖品{}触发熔断机制，实际回报率{}超过阈值{}", 
                         prize.getId(), actualRate, breakerThreshold);
                
                // 暂停奖品
                prizeConfigMapper.suspendPrize(
                    prize.getId(), 
                    "自动熔断：回报率异常", 
                    LocalDateTime.now().plusHours(1).toString()
                );
            }
        }
    }
    
    /**
     * 计算实际回报率
     */
    private BigDecimal calculateActualReturnRate(LotteryPrizeConfig prize) {
        if (prize.getTotalDrawCount() == null || prize.getTotalDrawCount() == 0) {
            return BigDecimal.ZERO;
        }
        
        // 实际回报率 = (中奖次数 * 奖品积分) / (总抽取次数 * 单次消耗积分)
        BigDecimal totalReward = BigDecimal.valueOf(
            prize.getTotalWinCount() != null ? prize.getTotalWinCount() : 0
        ).multiply(BigDecimal.valueOf(
            prize.getPrizePoints() != null ? prize.getPrizePoints() : 0
        ));
        
        BigDecimal totalCost = BigDecimal.valueOf(prize.getTotalDrawCount())
                .multiply(BigDecimal.valueOf(100)); // 假设单次消耗100积分
        
        if (totalCost.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return totalReward.divide(totalCost, 6, RoundingMode.HALF_UP);
    }
}

