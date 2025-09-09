package com.xiaou.notification.dto;

import lombok.Data;

/**
 * 消息统计响应DTO
 */
@Data
public class NotificationStatistics {
    
    /**
     * 今日发送总数
     */
    private Long todayTotal = 0L;
    
    /**
     * 本月发送总数
     */
    private Long monthTotal = 0L;
    
    /**
     * 未读消息总数
     */
    private Long unreadTotal = 0L;
    
    /**
     * 系统公告数量
     */
    private Long announcementCount = 0L;
    
    /**
     * 个人消息数量
     */
    private Long personalCount = 0L;
    
    /**
     * 社区互动消息数量
     */
    private Long communityCount = 0L;
    
    /**
     * 系统通知数量
     */
    private Long systemCount = 0L;
} 