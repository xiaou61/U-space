package com.xiaou.interview.dto;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 复习统计响应
 *
 * @author xiaou
 */
@Data
@Accessors(chain = true)
public class ReviewStatsResponse {

    /**
     * 逾期待复习数量
     */
    private Integer overdueCount;

    /**
     * 今日待复习数量
     */
    private Integer todayCount;

    /**
     * 本周待复习数量
     */
    private Integer weekCount;

    /**
     * 已学习总题数
     */
    private Integer totalLearned;

    /**
     * 不会的题目数
     */
    private Integer level1Count;

    /**
     * 模糊的题目数
     */
    private Integer level2Count;

    /**
     * 熟悉的题目数
     */
    private Integer level3Count;

    /**
     * 已掌握的题目数
     */
    private Integer level4Count;
}
