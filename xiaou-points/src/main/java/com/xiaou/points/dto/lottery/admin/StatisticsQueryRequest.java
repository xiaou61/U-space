package com.xiaou.points.dto.lottery.admin;

import lombok.Data;

/**
 * 统计查询请求
 * 
 * @author xiaou
 */
@Data
public class StatisticsQueryRequest {
    
    /**
     * 开始日期 (格式: yyyy-MM-dd)
     */
    private String startDate;
    
    /**
     * 结束日期 (格式: yyyy-MM-dd)
     */
    private String endDate;
}

