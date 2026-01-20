package com.xiaou.flashcard.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 学习提交结果响应VO
 *
 * @author xiaou
 */
@Data
public class FlashcardStudyResultVO {

    /**
     * 卡片ID
     */
    private Long cardId;

    /**
     * 更新后的掌握程度
     */
    private Integer masteryLevel;

    /**
     * 下次复习时间
     */
    private LocalDateTime nextReviewTime;

    /**
     * 间隔天数
     */
    private Integer intervalDays;

    /**
     * 今日剩余待复习数
     */
    private Integer remainingDue;

    /**
     * 今日剩余新卡数
     */
    private Integer remainingNew;
}
