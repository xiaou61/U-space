package com.xiaou.mockinterview.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户面试统计实体
 *
 * @author xiaou
 */
@Data
@Accessors(chain = true)
public class MockInterviewUserStats {

    /**
     * 统计ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

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
     * 连续面试天数
     */
    private Integer interviewStreak;

    /**
     * 最长连续天数
     */
    private Integer maxStreak;

    /**
     * 最后面试日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate lastInterviewDate;

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
