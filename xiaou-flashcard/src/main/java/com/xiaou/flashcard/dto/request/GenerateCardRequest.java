package com.xiaou.flashcard.dto.request;

import lombok.Data;

import java.util.List;

/**
 * 从题库生成闪卡请求
 *
 * @author xiaou
 */
@Data
public class GenerateCardRequest {

    /**
     * 面试题ID (单个生成时使用)
     */
    private Long questionId;

    /**
     * 面试题ID列表 (批量生成时使用)
     */
    private List<Long> questionIds;

    /**
     * 面试题分类ID (按分类批量生成时使用)
     */
    private Long categoryId;

    /**
     * 目标卡组ID
     */
    private Long deckId;
}
