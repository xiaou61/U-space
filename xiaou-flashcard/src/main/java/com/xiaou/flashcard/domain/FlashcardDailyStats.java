package com.xiaou.flashcard.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 闪卡每日学习统计实体类
 *
 * @author xiaou
 */
@Data
public class FlashcardDailyStats {

    /**
     * 统计ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 统计日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate statDate;

    /**
     * 新学卡片数
     */
    private Integer newCards;

    /**
     * 复习卡片数
     */
    private Integer reviewCards;

    /**
     * 正确卡片数
     */
    private Integer correctCards;

    /**
     * 学习时长(秒)
     */
    private Integer studyDuration;

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
