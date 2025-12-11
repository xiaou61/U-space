package com.xiaou.flashcard.dto.response;

import lombok.Data;

/**
 * 今日学习任务响应
 *
 * @author xiaou
 */
@Data
public class TodayStudyResponse {

    /**
     * 新卡数量
     */
    private Integer newCount;

    /**
     * 待复习数量
     */
    private Integer reviewCount;

    /**
     * 今日总任务
     */
    private Integer totalToday;

    /**
     * 今日已完成
     */
    private Integer completedToday;
}
