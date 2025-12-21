package com.xiaou.mockinterview.dto.response;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 面试统计响应DTO
 *
 * @author xiaou
 */
@Data
public class InterviewStatsResponse {

    /**
     * 总面试次数
     */
    private Integer totalInterviews;

    /**
     * 完成面试次数
     */
    private Integer completedInterviews;

    /**
     * 平均分
     */
    private BigDecimal avgScore;

    /**
     * 最高分
     */
    private Integer highestScore;

    /**
     * 总回答题数
     */
    private Integer totalQuestions;

    /**
     * 高分题数（>=7分）
     */
    private Integer correctQuestions;

    /**
     * 正确率
     */
    private BigDecimal correctRate;

    /**
     * 连续面试天数
     */
    private Integer interviewStreak;

    /**
     * 最长连续天数
     */
    private Integer maxStreak;

    /**
     * 完成率
     */
    private BigDecimal completionRate;
}
