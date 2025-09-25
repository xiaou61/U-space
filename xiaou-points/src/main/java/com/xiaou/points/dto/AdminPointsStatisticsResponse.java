package com.xiaou.points.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 管理端积分统计响应DTO
 * 
 * @author xiaou
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminPointsStatisticsResponse {
    
    /**
     * 总积分发放量
     */
    private Long totalPointsIssued;
    
    /**
     * 打卡获得积分总量
     */
    private Long checkinPointsSum;
    
    /**
     * 后台发放积分总量
     */
    private Long adminGrantPointsSum;
    
    /**
     * 活跃用户数（有积分余额的用户）
     */
    private Integer activeUserCount;
    
    /**
     * 打卡活跃用户数
     */
    private Integer checkinActiveUserCount;
    
    /**
     * 用户积分排行榜
     */
    private List<UserPointsRanking> userRankings;
    
    /**
     * 积分发放趋势（最近30天）
     */
    private List<DailyPointsTrend> dailyTrends;
    
    /**
     * 用户积分排行
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserPointsRanking {
        private Long userId;
        private String userName;
        private Integer totalPoints;
        private String balanceYuan;
        private Integer continuousDays;
        private Integer ranking;
    }
    
    /**
     * 每日积分趋势
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyPointsTrend {
        private String date;
        private Long checkinPoints;
        private Long grantPoints;
        private Long totalPoints;
        private Integer checkinUserCount;
    }
}
