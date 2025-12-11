package com.xiaou.flashcard.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 闪卡每日学习统计实体
 *
 * @author xiaou
 */
@Data
@Accessors(chain = true)
public class FlashcardDailyStats {

    /**
     * 记录ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 学习日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate studyDate;

    /**
     * 新学数量
     */
    private Integer newCount;

    /**
     * 复习数量
     */
    private Integer reviewCount;

    /**
     * 正确数量
     */
    private Integer correctCount;

    /**
     * 错误数量
     */
    private Integer wrongCount;

    /**
     * 学习时长(秒)
     */
    private Integer studyTimeSeconds;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
