package com.xiaou.flashcard.dto.response;

import lombok.Data;

/**
 * 学习统计概览响应
 *
 * @author xiaou
 */
@Data
public class StatsOverviewResponse {

    /**
     * 闪卡总数
     */
    private Integer totalCards;

    /**
     * 已掌握数量
     */
    private Integer masteredCards;

    /**
     * 学习中数量
     */
    private Integer learningCards;

    /**
     * 新卡数量
     */
    private Integer newCards;

    /**
     * 连续学习天数
     */
    private Integer streakDays;

    /**
     * 总复习次数
     */
    private Integer totalReviews;

    /**
     * 今日待复习数量
     */
    private Integer todayReviewCount;

    /**
     * 今日新卡数量
     */
    private Integer todayNewCount;

    /**
     * 今日已学数量
     */
    private Integer todayCompletedCount;
}
