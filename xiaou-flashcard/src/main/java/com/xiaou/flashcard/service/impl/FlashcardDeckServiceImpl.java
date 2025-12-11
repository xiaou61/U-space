package com.xiaou.flashcard.service.impl;

import com.xiaou.common.exception.BusinessException;
import com.xiaou.flashcard.domain.FlashcardDeck;
import com.xiaou.flashcard.domain.FlashcardStudyRecord;
import com.xiaou.flashcard.dto.request.CreateDeckRequest;
import com.xiaou.flashcard.mapper.FlashcardDeckMapper;
import com.xiaou.flashcard.mapper.FlashcardMapper;
import com.xiaou.flashcard.mapper.FlashcardStudyRecordMapper;
import com.xiaou.flashcard.service.FlashcardDeckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 闪卡卡组服务实现类
 *
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlashcardDeckServiceImpl implements FlashcardDeckService {

    private final FlashcardDeckMapper deckMapper;
    private final FlashcardMapper flashcardMapper;
    private final FlashcardStudyRecordMapper studyRecordMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDeck(Long userId, CreateDeckRequest request) {
        FlashcardDeck deck = new FlashcardDeck()
                .setUserId(userId)
                .setName(request.getName())
                .setDescription(request.getDescription())
                .setCoverImage(request.getCoverImage())
                .setCategoryId(request.getCategoryId())
                .setCategory(request.getCategory())
                .setCardCount(0)
                .setIsPublic(request.getIsPublic() != null ? request.getIsPublic() : 0)
                .setIsOfficial(userId == 0 ? 1 : 0) // userId=0表示管理员创建，设为官方卡组
                .setSortOrder(0)
                .setStatus(1)
                .setCreateTime(LocalDateTime.now())
                .setUpdateTime(LocalDateTime.now());

        deckMapper.insert(deck);
        log.info("创建卡组成功: userId={}, deckId={}, name={}", userId, deck.getId(), deck.getName());
        return deck.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDeck(Long userId, Long deckId, CreateDeckRequest request) {
        FlashcardDeck deck = deckMapper.selectById(deckId);
        if (deck == null) {
            throw new BusinessException("卡组不存在");
        }
        if (!deck.getUserId().equals(userId) && deck.getUserId() != 0) {
            throw new BusinessException("无权限修改此卡组");
        }

        deck.setName(request.getName())
            .setDescription(request.getDescription())
            .setCoverImage(request.getCoverImage())
            .setCategoryId(request.getCategoryId())
            .setIsPublic(request.getIsPublic())
            .setUpdateTime(LocalDateTime.now());

        deckMapper.updateById(deck);
        log.info("更新卡组成功: deckId={}", deckId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDeck(Long userId, Long deckId) {
        FlashcardDeck deck = deckMapper.selectById(deckId);
        if (deck == null) {
            throw new BusinessException("卡组不存在");
        }
        // userId=0表示管理员，可以删除任意卡组
        if (userId != 0 && !deck.getUserId().equals(userId)) {
            throw new BusinessException("无权限删除此卡组");
        }

        // 删除卡组下的所有闪卡
        flashcardMapper.deleteByDeckId(deckId);
        // 删除卡组
        deckMapper.deleteById(deckId);
        log.info("删除卡组成功: deckId={}", deckId);
    }

    @Override
    public FlashcardDeck getDeckById(Long deckId) {
        FlashcardDeck deck = deckMapper.selectById(deckId);
        if (deck == null) {
            throw new BusinessException("卡组不存在");
        }
        return deck;
    }

    @Override
    public List<FlashcardDeck> getUserDecks(Long userId) {
        return deckMapper.selectByUserId(userId);
    }

    @Override
    public List<FlashcardDeck> getOfficialDecks() {
        return deckMapper.selectOfficialDecks();
    }

    @Override
    public List<FlashcardDeck> getPublicDecks() {
        return deckMapper.selectPublicDecks();
    }

    @Override
    public List<FlashcardDeck> getUserDecksWithProgress(Long userId) {
        List<FlashcardDeck> decks = deckMapper.selectByUserId(userId);
        fillDeckProgress(decks, userId);
        return decks;
    }

    @Override
    public List<FlashcardDeck> getOfficialDecksWithProgress(Long userId) {
        List<FlashcardDeck> decks = deckMapper.selectOfficialDecks();
        fillDeckProgress(decks, userId);
        return decks;
    }

    /**
     * 填充卡组的学习进度数据
     */
    private void fillDeckProgress(List<FlashcardDeck> decks, Long userId) {
        LocalDate today = LocalDate.now();
        for (FlashcardDeck deck : decks) {
            // 已掌握数量
            int mastered = studyRecordMapper.countByDeckAndStatus(userId, deck.getId(), 
                    FlashcardStudyRecord.STATUS_MASTERED);
            // 学习中数量
            int learning = studyRecordMapper.countByDeckAndStatus(userId, deck.getId(), 
                    FlashcardStudyRecord.STATUS_LEARNING);
            // 待复习数量
            int reviewCount = studyRecordMapper.countDueReviewsByDeck(userId, deck.getId(), today);
            // 已学习过的卡片数
            int learned = studyRecordMapper.countLearnedByDeck(userId, deck.getId());
            // 新卡片数 = 总卡片数 - 已学习数
            int newCount = Math.max(0, (deck.getCardCount() != null ? deck.getCardCount() : 0) - learned);
            
            deck.setMasteredCount(mastered);
            deck.setLearningCount(learning);
            deck.setReviewCount(reviewCount);
            deck.setNewCount(newCount);
        }
    }

    @Override
    public void updateDeckCardCount(Long deckId) {
        int count = flashcardMapper.countByDeckId(deckId);
        deckMapper.updateCardCount(deckId, count);
    }
}
