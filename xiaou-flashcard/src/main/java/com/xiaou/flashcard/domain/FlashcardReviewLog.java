package com.xiaou.flashcard.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 闪卡复习详情日志实体
 *
 * @author xiaou
 */
@Data
@Accessors(chain = true)
public class FlashcardReviewLog {

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
     * 评分(0-3)
     */
    private Integer quality;

    /**
     * 复习前EF
     */
    private BigDecimal easeFactorBefore;

    /**
     * 复习后EF
     */
    private BigDecimal easeFactorAfter;

    /**
     * 复习前间隔
     */
    private Integer intervalBefore;

    /**
     * 复习后间隔
     */
    private Integer intervalAfter;

    /**
     * 复习时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime reviewTime;

    /**
     * 思考时长(毫秒)
     */
    private Integer timeSpentMs;
}
