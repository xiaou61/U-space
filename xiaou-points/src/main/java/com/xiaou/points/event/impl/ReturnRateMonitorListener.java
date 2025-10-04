package com.xiaou.points.event.impl;

import com.xiaou.points.domain.LotteryPrizeConfig;
import com.xiaou.points.event.LotteryEvent;
import com.xiaou.points.event.LotteryEventListener;
import com.xiaou.points.mapper.LotteryPrizeConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 回报率监控监听器
 * 
 * @author xiaou
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnRateMonitorListener implements LotteryEventListener {
    
    private final LotteryPrizeConfigMapper prizeConfigMapper;
    
    @Override
    public void onEvent(LotteryEvent event) {
        if (event.getType() != LotteryEvent.EventType.DRAW_COMPLETED) {
            return;
        }
        
        try {
            Long prizeId = event.getDrawRecord().getPrizeId();
            LotteryPrizeConfig config = prizeConfigMapper.selectById(prizeId);
            
            if (config == null || config.getTotalDrawCount() == 0) {
                return;
            }
            
            // 计算实际回报率
            BigDecimal actualRate = calculateActualReturnRate(config);
            
            // 更新实际回报率
            prizeConfigMapper.updateActualReturnRate(prizeId, actualRate);
            
            log.info("奖品{}实际回报率更新为: {}", prizeId, actualRate);
        } catch (Exception e) {
            log.error("更新回报率失败", e);
        }
    }
    
    /**
     * 计算实际回报率
     */
    private BigDecimal calculateActualReturnRate(LotteryPrizeConfig config) {
        if (config.getTotalDrawCount() == 0) {
            return BigDecimal.ZERO;
        }
        
        // 实际回报率 = (中奖次数 * 奖品积分) / (总抽取次数 * 单次消耗积分)
        BigDecimal totalReward = BigDecimal.valueOf(config.getTotalWinCount())
                .multiply(BigDecimal.valueOf(config.getPrizePoints()));
        
        // 假设单次消耗100积分（从常量获取）
        BigDecimal totalCost = BigDecimal.valueOf(config.getTotalDrawCount())
                .multiply(BigDecimal.valueOf(100));
        
        if (totalCost.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return totalReward.divide(totalCost, 6, RoundingMode.HALF_UP);
    }
    
    @Override
    public String getListenerName() {
        return "ReturnRateMonitorListener";
    }
}

