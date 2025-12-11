package com.xiaou.flashcard.service.impl;

import com.xiaou.common.exception.BusinessException;
import com.xiaou.flashcard.domain.Flashcard;
import com.xiaou.flashcard.dto.request.CreateCardRequest;
import com.xiaou.flashcard.dto.request.GenerateCardRequest;
import com.xiaou.flashcard.mapper.FlashcardDeckMapper;
import com.xiaou.flashcard.mapper.FlashcardMapper;
import com.xiaou.flashcard.service.FlashcardDeckService;
import com.xiaou.flashcard.service.FlashcardService;
import com.xiaou.interview.domain.InterviewQuestion;
import com.xiaou.interview.service.InterviewQuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 闪卡服务实现类
 *
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlashcardServiceImpl implements FlashcardService {

    private final FlashcardMapper flashcardMapper;
    private final FlashcardDeckMapper deckMapper;
    private final FlashcardDeckService deckService;
    private final InterviewQuestionService questionService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCard(Long userId, CreateCardRequest request) {
        // 验证卡组存在
        if (deckMapper.selectById(request.getDeckId()) == null) {
            throw new BusinessException("卡组不存在");
        }

        Flashcard card = new Flashcard()
                .setUserId(userId)
                .setDeckId(request.getDeckId())
                .setFrontContent(request.getFrontContent())
                .setBackContent(request.getBackContent())
                .setSourceType(2) // 用户创建
                .setStatus(1)
                .setCreateTime(LocalDateTime.now())
                .setUpdateTime(LocalDateTime.now());

        flashcardMapper.insert(card);
        // 更新卡组闪卡数量
        deckService.updateDeckCardCount(request.getDeckId());
        
        log.info("创建闪卡成功: userId={}, cardId={}", userId, card.getId());
        return card.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long generateFromQuestion(Long userId, GenerateCardRequest request) {
        // 检查是否已生成
        if (hasGeneratedFromQuestion(userId, request.getQuestionId())) {
            throw new BusinessException("该题目已生成过闪卡");
        }

        // 获取面试题详情
        InterviewQuestion question = questionService.getQuestionById(request.getQuestionId());
        if (question == null) {
            throw new BusinessException("面试题不存在");
        }

        // 验证卡组存在
        if (deckMapper.selectById(request.getDeckId()) == null) {
            throw new BusinessException("卡组不存在");
        }

        Flashcard card = new Flashcard()
                .setUserId(userId)
                .setDeckId(request.getDeckId())
                .setQuestionId(request.getQuestionId())
                .setFrontContent(question.getTitle())
                .setBackContent(question.getAnswer())
                .setSourceType(1) // 题库生成
                .setStatus(1)
                .setCreateTime(LocalDateTime.now())
                .setUpdateTime(LocalDateTime.now());

        flashcardMapper.insert(card);
        deckService.updateDeckCardCount(request.getDeckId());
        
        log.info("从面试题生成闪卡成功: userId={}, questionId={}, cardId={}", 
                userId, request.getQuestionId(), card.getId());
        return card.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int generateFromQuestionIds(Long userId, GenerateCardRequest request) {
        // 验证卡组存在
        if (deckMapper.selectById(request.getDeckId()) == null) {
            throw new BusinessException("卡组不存在");
        }

        int count = 0;
        for (Long questionId : request.getQuestionIds()) {
            // 跳过已生成的
            if (hasGeneratedFromQuestion(userId, questionId)) {
                continue;
            }

            // 获取面试题详情
            InterviewQuestion question = questionService.getQuestionById(questionId);
            if (question == null) {
                continue; // 跳过不存在的题目
            }

            Flashcard card = new Flashcard()
                    .setUserId(userId)
                    .setDeckId(request.getDeckId())
                    .setQuestionId(questionId)
                    .setFrontContent(question.getTitle())
                    .setBackContent(question.getAnswer())
                    .setSourceType(1)
                    .setStatus(1)
                    .setCreateTime(LocalDateTime.now())
                    .setUpdateTime(LocalDateTime.now());

            flashcardMapper.insert(card);
            count++;
        }

        deckService.updateDeckCardCount(request.getDeckId());
        log.info("批量生成闪卡成功: userId={}, count={}", userId, count);
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int generateFromCategory(Long userId, GenerateCardRequest request) {
        // 验证卡组存在
        if (deckMapper.selectById(request.getDeckId()) == null) {
            throw new BusinessException("卡组不存在");
        }

        // 获取分类下的所有题目
        List<InterviewQuestion> questions = questionService.getQuestionsBySetId(request.getCategoryId());
        if (questions.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (InterviewQuestion question : questions) {
            // 跳过已生成的
            if (hasGeneratedFromQuestion(userId, question.getId())) {
                continue;
            }

            Flashcard card = new Flashcard()
                    .setUserId(userId)
                    .setDeckId(request.getDeckId())
                    .setQuestionId(question.getId())
                    .setFrontContent(question.getTitle())
                    .setBackContent(question.getAnswer())
                    .setSourceType(1)
                    .setStatus(1)
                    .setCreateTime(LocalDateTime.now())
                    .setUpdateTime(LocalDateTime.now());

            flashcardMapper.insert(card);
            count++;
        }

        deckService.updateDeckCardCount(request.getDeckId());
        log.info("批量生成闪卡成功: userId={}, categoryId={}, count={}", 
                userId, request.getCategoryId(), count);
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCard(Long userId, Long cardId, CreateCardRequest request) {
        Flashcard card = flashcardMapper.selectById(cardId);
        if (card == null) {
            throw new BusinessException("闪卡不存在");
        }
        // userId=0表示管理员，可以修改任意闪卡
        if (userId != 0 && !card.getUserId().equals(userId)) {
            throw new BusinessException("无权限修改此闪卡");
        }

        card.setFrontContent(request.getFrontContent())
            .setBackContent(request.getBackContent())
            .setUpdateTime(LocalDateTime.now());

        flashcardMapper.updateById(card);
        log.info("更新闪卡成功: cardId={}", cardId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCard(Long userId, Long cardId) {
        Flashcard card = flashcardMapper.selectById(cardId);
        if (card == null) {
            throw new BusinessException("闪卡不存在");
        }
        // userId=0表示管理员，可以删除任意闪卡
        if (userId != 0 && !card.getUserId().equals(userId)) {
            throw new BusinessException("无权限删除此闪卡");
        }

        Long deckId = card.getDeckId();
        flashcardMapper.deleteById(cardId);
        deckService.updateDeckCardCount(deckId);
        log.info("删除闪卡成功: cardId={}", cardId);
    }

    @Override
    public Flashcard getCardById(Long cardId) {
        Flashcard card = flashcardMapper.selectById(cardId);
        if (card == null) {
            throw new BusinessException("闪卡不存在");
        }
        return card;
    }

    @Override
    public List<Flashcard> getCardsByDeckId(Long deckId) {
        return flashcardMapper.selectByDeckId(deckId);
    }

    @Override
    public List<Flashcard> getCardsByUserId(Long userId) {
        return flashcardMapper.selectByUserId(userId);
    }

    @Override
    public boolean hasGeneratedFromQuestion(Long userId, Long questionId) {
        Flashcard card = flashcardMapper.selectByQuestionId(questionId, userId);
        return card != null;
    }
}
