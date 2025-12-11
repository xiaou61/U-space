package com.xiaou.flashcard.dto.request;

import lombok.Data;

/**
 * 开始学习请求
 *
 * @author xiaou
 */
@Data
public class StudyStartRequest {

    /**
     * 卡组ID (可选，不指定则学习所有)
     */
    private Long deckId;

    /**
     * 学习模式: daily-每日任务 / free-自由学习 / weak-薄弱攻克
     */
    private String mode;

    /**
     * 每日新卡数量限制
     */
    private Integer newCardLimit;

    // 学习模式常量
    public static final String MODE_DAILY = "daily";
    public static final String MODE_FREE = "free";
    public static final String MODE_WEAK = "weak";
}
