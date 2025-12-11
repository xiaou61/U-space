package com.xiaou.flashcard.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 闪卡学习记录实体
 *
 * @author xiaou
 */
@Data
@Accessors(chain = true)
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
     * 难度因子(EF)
     */
    private BigDecimal easeFactor;

    /**
     * 当前间隔天数
     */
    private Integer intervalDays;

    /**
     * 连续正确次数
     */
    private Integer repetitions;

    /**
     * 最近一次评分(0-3)
     */
    private Integer quality;

    /**
     * 下次复习日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate nextReviewDate;

    /**
     * 最近复习时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastReviewTime;

    /**
     * 总复习次数
     */
    private Integer totalReviews;

    /**
     * 正确次数
     */
    private Integer correctCount;

    /**
     * 错误次数
     */
    private Integer wrongCount;

    /**
     * 状态: 0-新卡 1-学习中 2-已掌握
     */
    private Integer status;

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

    // ====== 状态常量 ======
    public static final int STATUS_NEW = 0;
    public static final int STATUS_LEARNING = 1;
    public static final int STATUS_MASTERED = 2;
}
