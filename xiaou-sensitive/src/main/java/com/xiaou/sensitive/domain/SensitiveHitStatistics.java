package com.xiaou.sensitive.domain;

import lombok.Data;

import java.time.LocalDate;

/**
 * 敏感词命中统计实体
 *
 * @author xiaou
 */
@Data
public class SensitiveHitStatistics {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 统计日期
     */
    private LocalDate statDate;

    /**
     * 敏感词
     */
    private String word;

    /**
     * 分类ID
     */
    private Integer categoryId;

    /**
     * 命中次数
     */
    private Integer hitCount;

    /**
     * 业务模块
     */
    private String module;
}
