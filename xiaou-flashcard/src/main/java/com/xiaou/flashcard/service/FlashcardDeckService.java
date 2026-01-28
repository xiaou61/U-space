package com.xiaou.flashcard.service;

import com.xiaou.flashcard.dto.request.FlashcardDeckCreateRequest;
import com.xiaou.flashcard.dto.request.FlashcardDeckUpdateRequest;
import com.xiaou.flashcard.dto.response.FlashcardDeckVO;

import java.util.List;

/**
 * 闪卡卡组服务接口
 *
 * @author xiaou
 */
public interface FlashcardDeckService {

    /**
     * 创建卡组
     */
    Long createDeck(FlashcardDeckCreateRequest request, Long userId);

    /**
     * 更新卡组
     */
    void updateDeck(FlashcardDeckUpdateRequest request, Long userId);

    /**
     * 删除卡组
     */
    void deleteDeck(Long id, Long userId);

    /**
     * 获取卡组详情
     */
    FlashcardDeckVO getDeckById(Long id, Long userId);

    /**
     * 获取用户的卡组列表
     */
    List<FlashcardDeckVO> getMyDecks(Long userId);

    /**
     * 获取公开卡组列表
     */
    List<FlashcardDeckVO> getPublicDecks(String keyword, String tags, Long userId);

    /**
     * 复制卡组
     */
    Long forkDeck(Long deckId, Long userId);
}
