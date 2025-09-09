package com.xiaou.moment.dto;

import lombok.Data;

/**
 * 每日统计数据
 */
@Data
public class DailyStatistics {
    
    /**
     * 日期
     */
    private String date;
    
    /**
     * 动态数量
     */
    private Long momentCount;
    
    /**
     * 点赞数量
     */
    private Long likeCount;
    
    /**
     * 评论数量
     */
    private Long commentCount;
    
    /**
     * 活跃用户数量
     */
    private Long activeUserCount;
} 