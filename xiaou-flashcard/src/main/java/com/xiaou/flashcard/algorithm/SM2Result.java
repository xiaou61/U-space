package com.xiaou.flashcard.algorithm;

import lombok.Data;

import java.math.BigDecimal;

/**
 * SM-2算法计算结果
 *
 * @author xiaou
 */
@Data
public class SM2Result {

    /**
     * 新的难度因子
     */
    private BigDecimal easeFactor;

    /**
     * 新的间隔天数
     */
    private Integer intervalDays;

    /**
     * 连续正确次数
     */
    private Integer repetitions;

    /**
     * 下次复习的分钟数（用于忘记情况，10分钟后复习）
     */
    private Integer nextReviewMinutes;

    /**
     * 是否需要立即复习（忘记的情况）
     */
    private Boolean needImmediateReview;

    /**
     * 新的学习状态: 0-新卡 1-学习中 2-已掌握
     */
    private Integer newStatus;
}
