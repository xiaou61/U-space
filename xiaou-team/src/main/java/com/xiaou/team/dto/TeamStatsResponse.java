package com.xiaou.team.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 小组统计响应DTO
 * 
 * @author xiaou
 */
@Data
public class TeamStatsResponse {
    
    /**
     * 小组ID
     */
    private Long teamId;
    
    /**
     * 小组名称
     */
    private String teamName;
    
    /**
     * 成员总数
     */
    private Integer memberCount;
    
    /**
     * 今日打卡人数
     */
    private Integer todayCheckinCount;
    
    /**
     * 今日打卡率
     */
    private Double todayCheckinRate;
    
    /**
     * 累计打卡次数
     */
    private Integer totalCheckins;
    
    /**
     * 累计打卡天数
     */
    private Integer totalCheckinDays;
    
    /**
     * 讨论数量
     */
    private Integer discussionCount;
    
    /**
     * 任务数量
     */
    private Integer taskCount;
    
    /**
     * 活跃任务数
     */
    private Integer activeTaskCount;
    
    /**
     * 本周打卡统计（按天）
     */
    private List<DailyStats> weeklyStats;
    
    /**
     * 本月打卡统计（按天）
     */
    private List<DailyStats> monthlyStats;
    
    /**
     * 每日统计
     */
    @Data
    public static class DailyStats {
        /**
         * 日期
         */
        @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
        private LocalDate date;
        
        /**
         * 打卡人数
         */
        private Integer checkinCount;
        
        /**
         * 打卡率
         */
        private Double checkinRate;
        
        /**
         * 总学习时长
         */
        private Integer totalDuration;
    }
    
    /**
     * 分类统计（任务类型分布）
     */
    private Map<String, Integer> taskTypeStats;
    
    /**
     * 用户个人统计
     */
    private UserStats userStats;
    
    /**
     * 用户个人统计
     */
    @Data
    public static class UserStats {
        /**
         * 用户ID
         */
        private Long userId;
        
        /**
         * 总打卡次数
         */
        private Integer totalCheckins;
        
        /**
         * 连续打卡天数
         */
        private Integer streakDays;
        
        /**
         * 本周打卡次数
         */
        private Integer weeklyCheckins;
        
        /**
         * 本月打卡次数
         */
        private Integer monthlyCheckins;
        
        /**
         * 总学习时长
         */
        private Integer totalDuration;
        
        /**
         * 排名
         */
        private Integer rank;
        
        /**
         * 贡献值
         */
        private Integer contribution;
    }
}
