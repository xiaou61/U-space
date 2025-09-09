package com.xiaou.moment.dto;

import lombok.Data;

/**
 * 统计查询条件
 */
@Data
public class StatisticsRequest {
    
    /**
     * 开始时间
     */
    private String startDate;
    
    /**
     * 结束时间
     */
    private String endDate;
    
    /**
     * 统计类型：daily/weekly/monthly
     */
    private String type;
} 