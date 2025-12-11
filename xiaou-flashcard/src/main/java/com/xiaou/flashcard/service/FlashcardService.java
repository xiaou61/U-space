package com.xiaou.flashcard.service;

import com.xiaou.flashcard.domain.Flashcard;
import com.xiaou.flashcard.dto.request.CreateCardRequest;
import com.xiaou.flashcard.dto.request.GenerateCardRequest;

import java.util.List;

/**
 * 闪卡服务接口
 *
 * @author xiaou
 */
public interface FlashcardService {

    /**
     * 创建闪卡
     */
    Long createCard(Long userId, CreateCardRequest request);

    /**
     * 从面试题生成闪卡
     */
    Long generateFromQuestion(Long userId, GenerateCardRequest request);

    /**
     * 从多个面试题批量生成闪卡
     */
    int generateFromQuestionIds(Long userId, GenerateCardRequest request);

    /**
     * 从分类批量生成闪卡
     */
    int generateFromCategory(Long userId, GenerateCardRequest request);

    /**
     * 更新闪卡
     */
    void updateCard(Long userId, Long cardId, CreateCardRequest request);

    /**
     * 删除闪卡
     */
    void deleteCard(Long userId, Long cardId);

    /**
     * 获取闪卡详情
     */
    Flashcard getCardById(Long cardId);

    /**
     * 获取卡组的闪卡列表
     */
    List<Flashcard> getCardsByDeckId(Long deckId);

    /**
     * 获取用户的闪卡列表
     */
    List<Flashcard> getCardsByUserId(Long userId);

    /**
     * 检查是否已从该面试题生成闪卡
     */
    boolean hasGeneratedFromQuestion(Long userId, Long questionId);
}
