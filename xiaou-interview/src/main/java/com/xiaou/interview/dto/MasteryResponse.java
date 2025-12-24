package com.xiaou.interview.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 掌握度响应
 *
 * @author xiaou
 */
@Data
@Accessors(chain = true)
public class MasteryResponse {

    /**
     * 题目ID
     */
    private Long questionId;

    /**
     * 掌握度等级 1-不会 2-模糊 3-熟悉 4-已掌握
     */
    private Integer masteryLevel;

    /**
     * 掌握度等级文本
     */
    private String masteryLevelText;

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
     * 下次复习时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime nextReviewTime;

    /**
     * 距离下次复习的天数
     */
    private Integer nextReviewDays;

    /**
     * 是否逾期
     */
    private Boolean isOverdue;

    /**
     * 获取掌握度等级文本
     */
    public static String getMasteryLevelText(Integer level) {
        if (level == null) return "未学习";
        switch (level) {
            case 1: return "不会";
            case 2: return "模糊";
            case 3: return "熟悉";
            case 4: return "已掌握";
            default: return "未知";
        }
    }
}
