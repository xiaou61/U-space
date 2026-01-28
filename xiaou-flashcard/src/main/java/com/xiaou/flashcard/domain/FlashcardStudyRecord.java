package com.xiaou.flashcard.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 闪卡学习记录实体类
 * 存储 SM-2 算法的核心数据
 *
 * @author xiaou
 */
@Data
public class FlashcardStudyRecord {

    /**
     * 记录ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 闪卡ID
     */
    private Long cardId;

    /**
     * 卡组ID
     */
    private Long deckId;

    /**
     * 连续正确次数
     */
    private Integer repetitions;

    /**
     * 难度因子(EF)，SM-2算法核心参数
     * 初始值2.5，最小值1.3
     */
    private Double easeFactor;

    /**
     * 当前间隔天数
     */
    private Integer intervalDays;

    /**
     * 掌握度: 1-新卡 2-学习中 3-已掌握
     */
    private Integer masteryLevel;

    /**
     * 上次复习时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastReviewTime;

    /**
     * 下次复习时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime nextReviewTime;

    /**
     * 总复习次数
     */
    private Integer totalReviews;

    /**
     * 正确次数
     */
    private Integer correctCount;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
