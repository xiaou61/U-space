package com.xiaou.flashcard.dto.response;

import lombok.Data;

/**
 * 学习统计响应VO
 *
 * @author xiaou
 */
@Data
public class FlashcardStudyStatsVO {

    /**
     * 今日待学新卡数
     */
    private Integer todayNewCount;

    /**
     * 今日待复习数
     */
    private Integer todayDueCount;

    /**
     * 今日已学习数
     */
    private Integer todayLearnedCount;

    /**
     * 总学习卡片数
     */
    private Integer totalLearnedCount;

    /**
     * 新学卡片数
     */
    private Integer newCount;

    /**
     * 学习中卡片数
     */
    private Integer learningCount;

    /**
     * 已掌握卡片数
     */
    private Integer masteredCount;

    /**
     * 总学习天数
     */
    private Integer totalStudyDays;

    /**
     * 连续学习天数
     */
    private Integer streakDays;

    /**
     * 今日学习时长（分钟）
     */
    private Integer todayDuration;
}
