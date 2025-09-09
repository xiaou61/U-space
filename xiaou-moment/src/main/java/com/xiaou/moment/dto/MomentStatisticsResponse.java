package com.xiaou.moment.dto;

import lombok.Data;

import java.util.List;

/**
 * 动态统计数据响应
 */
@Data
public class MomentStatisticsResponse {
    
    /**
     * 总动态数
     */
    private Long totalMoments;
    
    /**
     * 总点赞数  
     */
    private Long totalLikes;
    
    /**
     * 总评论数
     */
    private Long totalComments;
    
    /**
     * 活跃用户数
     */
    private Long activeUsers;
    
    /**
     * 每日统计数据
     */
    private List<DailyStatistics> dailyStats;
} 