package com.xiaou.flashcard.dto.request;

import lombok.Data;

/**
 * 提交复习反馈请求
 *
 * @author xiaou
 */
@Data
public class ReviewSubmitRequest {

    /**
     * 闪卡ID
     */
    private Long cardId;

    /**
     * 评分: 0-忘记 1-模糊 2-记得 3-简单
     */
    private Integer quality;

    /**
     * 思考时长(毫秒)
     */
    private Integer timeSpentMs;

    // 评分常量
    public static final int QUALITY_FORGOT = 0;
    public static final int QUALITY_HARD = 1;
    public static final int QUALITY_GOOD = 2;
    public static final int QUALITY_EASY = 3;
}
