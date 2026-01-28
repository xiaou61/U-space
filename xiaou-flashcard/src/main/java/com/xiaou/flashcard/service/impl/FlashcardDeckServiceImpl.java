package com.xiaou.flashcard.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.xiaou.common.exception.BusinessException;
import com.xiaou.flashcard.domain.Flashcard;
import com.xiaou.flashcard.domain.FlashcardDeck;
import com.xiaou.flashcard.dto.request.FlashcardDeckCreateRequest;
import com.xiaou.flashcard.dto.request.FlashcardDeckUpdateRequest;
import com.xiaou.flashcard.dto.response.FlashcardDeckVO;
import com.xiaou.flashcard.mapper.FlashcardDeckMapper;
import com.xiaou.flashcard.mapper.FlashcardMapper;
import com.xiaou.flashcard.mapper.FlashcardStudyRecordMapper;
import com.xiaou.flashcard.service.FlashcardDeckService;
import com.xiaou.user.api.UserInfoApiService;
import com.xiaou.user.api.dto.SimpleUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 闪卡卡组服务实现
 *
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlashcardDeckServiceImpl implements FlashcardDeckService {

    private final FlashcardDeckMapper deckMapper;
    private final FlashcardMapper cardMapper;
    private final FlashcardStudyRecordMapper studyRecordMapper;
    private final UserInfoApiService userInfoApiService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDeck(FlashcardDeckCreateRequest request, Long userId) {
        FlashcardDeck deck = new FlashcardDeck();
        BeanUtil.copyProperties(request, deck);
        deck.setUserId(userId);
        deck.setCardCount(0);
        deck.setStudyCount(0);
        deck.setForkCount(0);
        deck.setCreateTime(LocalDateTime.now());
        deck.setUpdateTime(LocalDateTime.now());
        deck.setDelFlag(0);
        deckMapper.insert(deck);
        return deck.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDeck(FlashcardDeckUpdateRequest request, Long userId) {
        FlashcardDeck deck = deckMapper.selectById(request.getId());
        if (deck == null) {
            throw new BusinessException("卡组不存在");
        }
        if (!Objects.equals(deck.getUserId(), userId)) {
            throw new BusinessException("无权修改此卡组");
        }

        if (request.getName() != null) {
            deck.setName(request.getName());
        }
        if (request.getDescription() != null) {
            deck.setDescription(request.getDescription());
        }
        if (request.getCoverImage() != null) {
            deck.setCoverImage(request.getCoverImage());
        }
        if (request.getTags() != null) {
            deck.setTags(request.getTags());
        }
        if (request.getIsPublic() != null) {
            deck.setIsPublic(request.getIsPublic());
        }
        deck.setUpdateTime(LocalDateTime.now());
        deckMapper.update(deck);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDeck(Long id, Long userId) {
        FlashcardDeck deck = deckMapper.selectById(id);
        if (deck == null) {
            throw new BusinessException("卡组不存在");
        }
        if (!Objects.equals(deck.getUserId(), userId)) {
            throw new BusinessException("无权删除此卡组");
        }
        // 删除卡组下的所有闪卡
        cardMapper.deleteByDeckId(id);
        // 删除卡组
        deckMapper.deleteById(id);
    }

    @Override
    public FlashcardDeckVO getDeckById(Long id, Long userId) {
        FlashcardDeck deck = deckMapper.selectById(id);
        if (deck == null) {
            throw new BusinessException("卡组不存在");
        }
        return convertToVO(deck, userId);
    }

    @Override
    public List<FlashcardDeckVO> getMyDecks(Long userId) {
        List<FlashcardDeck> decks = deckMapper.selectByUserId(userId);
        return decks.stream()
                .map(deck -> convertToVO(deck, userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<FlashcardDeckVO> getPublicDecks(String keyword, String tags, Long userId) {
        List<FlashcardDeck> decks = deckMapper.selectPublicDecks(keyword, tags);
        return decks.stream()
                .map(deck -> convertToVO(deck, userId))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long forkDeck(Long deckId, Long userId) {
        FlashcardDeck sourceDeck = deckMapper.selectById(deckId);
        if (sourceDeck == null) {
            throw new BusinessException("卡组不存在");
        }
        if (!sourceDeck.getIsPublic() && !Objects.equals(sourceDeck.getUserId(), userId)) {
            throw new BusinessException("无权复制此卡组");
        }

        // 创建新卡组
        FlashcardDeck newDeck = new FlashcardDeck();
        newDeck.setUserId(userId);
        newDeck.setName(sourceDeck.getName());
        newDeck.setDescription(sourceDeck.getDescription());
        newDeck.setCoverImage(sourceDeck.getCoverImage());
        newDeck.setTags(sourceDeck.getTags());
        newDeck.setIsPublic(false);
        newDeck.setCardCount(0);
        newDeck.setStudyCount(0);
        newDeck.setForkCount(0);
        newDeck.setSourceDeckId(deckId);
        newDeck.setCreateTime(LocalDateTime.now());
        newDeck.setUpdateTime(LocalDateTime.now());
        newDeck.setDelFlag(0);
        deckMapper.insert(newDeck);

        // 复制闪卡
        List<Flashcard> sourceCards = cardMapper.selectByDeckId(deckId);
        if (!sourceCards.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            List<Flashcard> newCards = sourceCards.stream().map(card -> {
                Flashcard newCard = new Flashcard();
                newCard.setDeckId(newDeck.getId());
                newCard.setFrontContent(card.getFrontContent());
                newCard.setBackContent(card.getBackContent());
                newCard.setContentType(card.getContentType());
                newCard.setTags(card.getTags());
                newCard.setSourceQuestionId(card.getSourceQuestionId());
                newCard.setSortOrder(card.getSortOrder());
                newCard.setCreateTime(now);
                newCard.setUpdateTime(now);
                newCard.setDelFlag(0);
                return newCard;
            }).collect(Collectors.toList());
            cardMapper.batchInsert(newCards);
            // 更新卡片数量
            deckMapper.updateCardCount(newDeck.getId(), newCards.size());
        }

        // 增加源卡组的fork次数
        deckMapper.incrementForkCount(deckId);

        return newDeck.getId();
    }

    private FlashcardDeckVO convertToVO(FlashcardDeck deck, Long userId) {
        FlashcardDeckVO vo = new FlashcardDeckVO();
        BeanUtil.copyProperties(deck, vo);

        // 获取用户信息
        try {
            SimpleUserInfo user = userInfoApiService.getSimpleUserInfo(deck.getUserId());
            if (user != null) {
                vo.setUserName(user.getDisplayName());
                vo.setUserAvatar(user.getAvatar());
            }
        } catch (Exception e) {
            log.warn("获取用户信息失败: {}", e.getMessage());
        }

        // 获取当前用户的学习进度
        if (userId != null) {
            int learnedCount = studyRecordMapper.countLearnedCards(userId, deck.getId());
            int dueCount = studyRecordMapper.countDueCards(userId, deck.getId(), LocalDateTime.now());
            vo.setLearnedCount(learnedCount);
            vo.setDueCount(dueCount);
        }

        return vo;
    }
}
