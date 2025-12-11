package com.xiaou.flashcard.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

/**
 * 复习结果响应
 *
 * @author xiaou
 */
@Data
public class ReviewResultResponse {

    /**
     * 下次复习日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate nextReviewDate;

    /**
     * 间隔天数
     */
    private Integer intervalDays;

    /**
     * 间隔描述
     */
    private String intervalDescription;

    /**
     * 是否还有下一张
     */
    private Boolean hasNext;

    /**
     * 今日剩余数量
     */
    private Integer remainingToday;

    /**
     * 新的学习状态
     */
    private Integer newStatus;

    /**
     * 状态描述
     */
    private String statusDescription;
}
