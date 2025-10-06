package com.xiaou.sensitive.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 敏感词统计查询DTO
 *
 * @author xiaou
 */
@Data
public class SensitiveStatisticsQuery {

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 业务模块
     */
    private String module;

    /**
     * 分类ID
     */
    private Integer categoryId;

    /**
     * TOP数量（用于热词查询）
     */
    private Integer topN;
}
