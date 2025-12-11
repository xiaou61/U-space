package com.xiaou.flashcard.dto.response;

import lombok.Data;

/**
 * 学习闪卡响应
 *
 * @author xiaou
 */
@Data
public class StudyCardResponse {

    /**
     * 闪卡ID
     */
    private Long cardId;

    /**
     * 正面内容(问题)
     */
    private String frontContent;

    /**
     * 背面内容(答案)
     */
    private String backContent;

    /**
     * 卡组ID
     */
    private Long deckId;

    /**
     * 卡组名称
     */
    private String deckName;

    /**
     * 是否新卡
     */
    private Boolean isNew;

    /**
     * 复习次数
     */
    private Integer reviewCount;

    /**
     * 当前进度
     */
    private Integer current;

    /**
     * 总数
     */
    private Integer total;
}
