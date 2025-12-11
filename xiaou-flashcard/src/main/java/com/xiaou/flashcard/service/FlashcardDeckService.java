package com.xiaou.flashcard.service;

import com.xiaou.flashcard.domain.FlashcardDeck;
import com.xiaou.flashcard.dto.request.CreateDeckRequest;

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
    Long createDeck(Long userId, CreateDeckRequest request);

    /**
     * 更新卡组
     */
    void updateDeck(Long userId, Long deckId, CreateDeckRequest request);

    /**
     * 删除卡组
     */
    void deleteDeck(Long userId, Long deckId);

    /**
     * 获取卡组详情
     */
    FlashcardDeck getDeckById(Long deckId);

    /**
     * 获取用户的卡组列表
     */
    List<FlashcardDeck> getUserDecks(Long userId);

    /**
     * 获取官方卡组列表
     */
    List<FlashcardDeck> getOfficialDecks();

    /**
     * 获取公开卡组列表
     */
    List<FlashcardDeck> getPublicDecks();

    /**
     * 获取用户的卡组列表(含学习进度)
     */
    List<FlashcardDeck> getUserDecksWithProgress(Long userId);

    /**
     * 获取官方卡组列表(含学习进度)
     */
    List<FlashcardDeck> getOfficialDecksWithProgress(Long userId);

    /**
     * 更新卡组的闪卡数量
     */
    void updateDeckCardCount(Long deckId);
}
