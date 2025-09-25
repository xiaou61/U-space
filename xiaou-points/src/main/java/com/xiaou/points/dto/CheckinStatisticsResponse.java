package com.xiaou.points.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 打卡统计响应DTO
 * 
 * @author xiaou
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckinStatisticsResponse {
    
    /**
     * 总打卡天数
     */
    private Integer totalCheckinDays;
    
    /**
     * 当前连续打卡天数
     */
    private Integer currentContinuousDays;
    
    /**
     * 历史最大连续打卡天数
     */
    private Integer maxContinuousDays;
    
    /**
     * 打卡获得的总积分
     */
    private Integer totalPointsFromCheckin;
    
    /**
     * 平均每日获得积分
     */
    private Double averagePointsPerDay;
    
    /**
     * 按月统计数据
     */
    private List<MonthlyCheckinStats> monthlyStats;
    
    /**
     * 月度打卡统计
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyCheckinStats {
        private String yearMonth;
        private Integer checkinDays;
        private Integer pointsEarned;
        private String checkinRate;
    }
}
