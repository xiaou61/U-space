package com.xiaou.interview.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 面试题掌握度记录
 *
 * @author xiaou
 */
@Data
@Accessors(chain = true)
public class InterviewMasteryRecord {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 题目ID
     */
    private Long questionId;

    /**
     * 题单ID
     */
    private Long questionSetId;

    /**
     * 掌握度等级 1-不会 2-模糊 3-熟悉 4-已掌握
     */
    private Integer masteryLevel;

    /**
     * 复习次数
     */
    private Integer reviewCount;

    /**
     * 上次复习时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastReviewTime;

    /**
     * 下次应复习时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime nextReviewTime;

    /**
     * 首次学习时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    // ========== 查询时使用的扩展字段 ==========

    /**
     * 题目标题
     */
    private String questionTitle;

    /**
     * 题单标题
     */
    private String questionSetTitle;

    /**
     * 逾期天数
     */
    private Integer overdueDays;
}
