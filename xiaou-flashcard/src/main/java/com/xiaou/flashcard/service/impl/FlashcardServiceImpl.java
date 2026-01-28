package com.xiaou.flashcard.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.xiaou.common.exception.BusinessException;
import com.xiaou.flashcard.domain.Flashcard;
import com.xiaou.flashcard.domain.FlashcardDeck;
import com.xiaou.flashcard.domain.FlashcardStudyRecord;
import com.xiaou.flashcard.dto.request.FlashcardBatchCreateRequest;
import com.xiaou.flashcard.dto.request.FlashcardCreateRequest;
import com.xiaou.flashcard.dto.request.FlashcardImportRequest;
import com.xiaou.flashcard.dto.response.FlashcardVO;
import com.xiaou.flashcard.enums.CardContentType;
import com.xiaou.flashcard.mapper.FlashcardDeckMapper;
import com.xiaou.flashcard.mapper.FlashcardMapper;
import com.xiaou.flashcard.mapper.FlashcardStudyRecordMapper;
import com.xiaou.flashcard.service.FlashcardService;
import com.xiaou.interview.domain.InterviewQuestion;
import com.xiaou.interview.service.InterviewQuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 闪卡服务实现
 *
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlashcardServiceImpl implements FlashcardService {

    private final FlashcardMapper cardMapper;
    private final FlashcardDeckMapper deckMapper;
    private final FlashcardStudyRecordMapper studyRecordMapper;
    private final InterviewQuestionService interviewQuestionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCard(FlashcardCreateRequest request, Long userId) {
        FlashcardDeck deck = validateDeckOwnership(request.getDeckId(), userId);

        Flashcard card = new Flashcard();
        BeanUtil.copyProperties(request, card);
        card.setCreateTime(LocalDateTime.now());
        card.setUpdateTime(LocalDateTime.now());
        card.setDelFlag(0);

        if (card.getSortOrder() == null) {
            card.setSortOrder(cardMapper.countByDeckId(deck.getId()) + 1);
        }

        cardMapper.insert(card);
        deckMapper.updateCardCount(deck.getId(), 1);
        return card.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchCreateCards(FlashcardBatchCreateRequest request, Long userId) {
        FlashcardDeck deck = validateDeckOwnership(request.getDeckId(), userId);

        LocalDateTime now = LocalDateTime.now();
        int startOrder = cardMapper.countByDeckId(deck.getId()) + 1;

        List<Flashcard> cards = request.getCards().stream().map(item -> {
            Flashcard card = new Flashcard();
            card.setDeckId(request.getDeckId());
            card.setFrontContent(item.getFrontContent());
            card.setBackContent(item.getBackContent());
            card.setContentType(item.getContentType() != null ? item.getContentType() : 1);
            card.setTags(item.getTags());
            card.setCreateTime(now);
            card.setUpdateTime(now);
            card.setDelFlag(0);
            return card;
        }).collect(Collectors.toList());

        // 设置排序
        for (int i = 0; i < cards.size(); i++) {
            cards.get(i).setSortOrder(startOrder + i);
        }

        cardMapper.batchInsert(cards);
        deckMapper.updateCardCount(deck.getId(), cards.size());
        return cards.size();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCard(Long id, FlashcardCreateRequest request, Long userId) {
        Flashcard card = cardMapper.selectById(id);
        if (card == null) {
            throw new BusinessException("闪卡不存在");
        }
        validateDeckOwnership(card.getDeckId(), userId);

        if (request.getFrontContent() != null) {
            card.setFrontContent(request.getFrontContent());
        }
        if (request.getBackContent() != null) {
            card.setBackContent(request.getBackContent());
        }
        if (request.getContentType() != null) {
            card.setContentType(request.getContentType());
        }
        if (request.getTags() != null) {
            card.setTags(request.getTags());
        }
        if (request.getSortOrder() != null) {
            card.setSortOrder(request.getSortOrder());
        }
        card.setUpdateTime(LocalDateTime.now());
        cardMapper.update(card);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCard(Long id, Long userId) {
        Flashcard card = cardMapper.selectById(id);
        if (card == null) {
            throw new BusinessException("闪卡不存在");
        }
        validateDeckOwnership(card.getDeckId(), userId);

        cardMapper.deleteById(id);
        deckMapper.updateCardCount(card.getDeckId(), -1);
    }

    @Override
    public List<FlashcardVO> getCardsByDeckId(Long deckId, Long userId) {
        FlashcardDeck deck = deckMapper.selectById(deckId);
        if (deck == null) {
            throw new BusinessException("卡组不存在");
        }
        // 公开卡组或自己的卡组可以查看
        if (!deck.getIsPublic() && !Objects.equals(deck.getUserId(), userId)) {
            throw new BusinessException("无权查看此卡组");
        }

        List<Flashcard> cards = cardMapper.selectByDeckId(deckId);

        // 获取用户的学习记录
        Map<Long, FlashcardStudyRecord> recordMap = null;
        if (userId != null) {
            List<FlashcardStudyRecord> records = studyRecordMapper.selectByUserAndDeck(userId, deckId);
            recordMap = records.stream()
                    .collect(Collectors.toMap(FlashcardStudyRecord::getCardId, Function.identity()));
        }

        Map<Long, FlashcardStudyRecord> finalRecordMap = recordMap;
        return cards.stream().map(card -> {
            FlashcardVO vo = new FlashcardVO();
            BeanUtil.copyProperties(card, vo);

            if (finalRecordMap != null && finalRecordMap.containsKey(card.getId())) {
                FlashcardStudyRecord record = finalRecordMap.get(card.getId());
                FlashcardVO.StudyStatusVO status = new FlashcardVO.StudyStatusVO();
                status.setMasteryLevel(record.getMasteryLevel());
                status.setNextReviewTime(record.getNextReviewTime());
                status.setTotalReviews(record.getTotalReviews());
                status.setCorrectCount(record.getCorrectCount());
                vo.setStudyStatus(status);
            }

            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int importFromQuestionBank(FlashcardImportRequest request, Long userId) {
        FlashcardDeck deck = validateDeckOwnership(request.getDeckId(), userId);

        List<Long> questionIds = request.getQuestionIds();
        if (questionIds == null || questionIds.isEmpty()) {
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        int startOrder = cardMapper.countByDeckId(deck.getId()) + 1;
        List<Flashcard> cardsToInsert = new ArrayList<>();
        int importedCount = 0;

        for (Long questionId : questionIds) {
            // 检查是否已导入
            if (cardMapper.countBySourceQuestionId(deck.getId(), questionId) > 0) {
                log.debug("题目 {} 已导入到卡组 {}，跳过", questionId, deck.getId());
                continue;
            }

            // 获取题目详情
            InterviewQuestion question = interviewQuestionService.getQuestionById(questionId);
            if (question == null) {
                log.warn("题目 {} 不存在，跳过", questionId);
                continue;
            }

            // 创建闪卡
            Flashcard card = new Flashcard();
            card.setDeckId(deck.getId());
            card.setFrontContent(question.getTitle());  // 题目作为正面
            card.setBackContent(question.getAnswer());  // 答案作为反面
            card.setContentType(CardContentType.MARKDOWN.getCode());  // 答案是Markdown格式
            card.setSourceQuestionId(questionId);
            card.setSortOrder(startOrder + importedCount);
            card.setCreateTime(now);
            card.setUpdateTime(now);
            card.setDelFlag(0);

            cardsToInsert.add(card);
            importedCount++;
        }

        // 批量插入
        if (!cardsToInsert.isEmpty()) {
            cardMapper.batchInsert(cardsToInsert);
            deckMapper.updateCardCount(deck.getId(), cardsToInsert.size());
        }

        log.info("用户 {} 从题库导入 {} 张闪卡到卡组 {}", userId, importedCount, deck.getId());
        return importedCount;
    }

    private FlashcardDeck validateDeckOwnership(Long deckId, Long userId) {
        FlashcardDeck deck = deckMapper.selectById(deckId);
        if (deck == null) {
            throw new BusinessException("卡组不存在");
        }
        if (!Objects.equals(deck.getUserId(), userId)) {
            throw new BusinessException("无权操作此卡组");
        }
        return deck;
    }
}
