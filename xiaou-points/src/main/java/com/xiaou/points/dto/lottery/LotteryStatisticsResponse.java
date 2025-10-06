package com.xiaou.points.dto.lottery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户抽奖统计响应DTO
 * 
 * @author xiaou
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotteryStatisticsResponse {
    
    /**
     * 总抽奖次数
     */
    private Integer totalDrawCount;
    
    /**
     * 总中奖次数
     */
    private Integer totalWinCount;
    
    /**
     * 中奖率
     */
    private String winRate;
    
    /**
     * 总消耗积分
     */
    private Long totalCostPoints;
    
    /**
     * 总获得积分
     */
    private Long totalRewardPoints;
    
    /**
     * 净收益
     */
    private Long netProfit;
    
    /**
     * 今日抽奖次数
     */
    private Integer todayDrawCount;
    
    /**
     * 今日中奖次数
     */
    private Integer todayWinCount;
    
    /**
     * 当前连续未中奖次数
     */
    private Integer currentContinuousNoWin;
    
    /**
     * 最大连续未中奖次数
     */
    private Integer maxContinuousNoWin;
}

