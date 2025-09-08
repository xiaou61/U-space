package com.xiaou.notification.dto;

import lombok.Data;

/**
 * 消息统计查询请求DTO
 */
@Data
public class StatisticsRequest {
    
    /**
     * 开始时间
     */
    private String startTime;
    
    /**
     * 结束时间  
     */
    private String endTime;
    
    /**
     * 消息类型筛选
     */
    private String type;
    
    /**
     * 统计维度：day(按天)/month(按月)/total(总计)
     */
    private String dimension = "total";
} 