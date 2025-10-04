package com.xiaou.points.event.impl;

import com.xiaou.points.event.LotteryEvent;
import com.xiaou.points.event.LotteryEventListener;
import com.xiaou.points.mapper.LotteryPrizeConfigMapper;
import com.xiaou.points.mapper.LotteryStatisticsDailyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 统计监听器
 * 
 * @author xiaou
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StatisticsListener implements LotteryEventListener {
    
    private final LotteryStatisticsDailyMapper statisticsMapper;
    private final LotteryPrizeConfigMapper prizeConfigMapper;
    
    @Override
    public void onEvent(LotteryEvent event) {
        if (event.getType() != LotteryEvent.EventType.DRAW_COMPLETED) {
            return;
        }
        
        try {
            // 更新今日统计
            statisticsMapper.incrementTodayDraw();
            
            // 更新奖品配置统计
            Long prizeId = event.getDrawRecord().getPrizeId();
            prizeConfigMapper.incrementDrawCount(prizeId);
            
            // 如果中奖，增加中奖次数
            if (event.getDrawRecord().getPrizeLevel() < 8) {
                statisticsMapper.incrementTodayWin();
                prizeConfigMapper.incrementWinCount(prizeId);
            }
            
            log.info("统计数据更新成功");
        } catch (Exception e) {
            log.error("更新统计数据失败", e);
        }
    }
    
    @Override
    public String getListenerName() {
        return "StatisticsListener";
    }
}

