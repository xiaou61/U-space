package com.xiaou.sensitive.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 统计总览VO
 *
 * @author xiaou
 */
@Data
@Builder
public class StatisticsOverviewVO {

    /**
     * 总检测数
     */
    private Long totalCheck;

    /**
     * 命中数
     */
    private Long hitCount;

    /**
     * 拦截率（%）
     */
    private Double hitRate;

    /**
     * 拒绝数
     */
    private Long rejectCount;

    /**
     * 替换数
     */
    private Long replaceCount;

    /**
     * 今日新增敏感词数
     */
    private Integer todayNewWords;

    /**
     * 违规用户数
     */
    private Long violationUserCount;
}
