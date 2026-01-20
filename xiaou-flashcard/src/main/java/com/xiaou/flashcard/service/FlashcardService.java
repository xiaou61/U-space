package com.xiaou.flashcard.service;

import com.xiaou.flashcard.dto.request.FlashcardBatchCreateRequest;
import com.xiaou.flashcard.dto.request.FlashcardCreateRequest;
import com.xiaou.flashcard.dto.request.FlashcardImportRequest;
import com.xiaou.flashcard.dto.response.FlashcardVO;

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
    Long createCard(FlashcardCreateRequest request, Long userId);

    /**
     * 批量创建闪卡
     */
    int batchCreateCards(FlashcardBatchCreateRequest request, Long userId);

    /**
     * 更新闪卡
     */
    void updateCard(Long id, FlashcardCreateRequest request, Long userId);

    /**
     * 删除闪卡
     */
    void deleteCard(Long id, Long userId);

    /**
     * 获取卡组内的闪卡列表
     */
    List<FlashcardVO> getCardsByDeckId(Long deckId, Long userId);

    /**
     * 从面试题库导入闪卡
     */
    int importFromQuestionBank(FlashcardImportRequest request, Long userId);
}
