package com.xiaou.flashcard.service.impl;

import com.xiaou.common.exception.BusinessException;
import com.xiaou.flashcard.algorithm.SM2Algorithm;
import com.xiaou.flashcard.algorithm.SM2Result;
import com.xiaou.flashcard.domain.*;
import com.xiaou.flashcard.dto.request.ReviewSubmitRequest;
import com.xiaou.flashcard.dto.request.StudyStartRequest;
import com.xiaou.flashcard.dto.response.ReviewResultResponse;
import com.xiaou.flashcard.dto.response.StudyCardResponse;
import com.xiaou.flashcard.dto.response.TodayStudyResponse;
import com.xiaou.flashcard.mapper.*;
import com.xiaou.flashcard.service.FlashcardStudyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 闪卡学习服务实现类
 *
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlashcardStudyServiceImpl implements FlashcardStudyService {

    private final FlashcardMapper flashcardMapper;
    private final FlashcardStudyRecordMapper studyRecordMapper;
    private final FlashcardDailyStatsMapper dailyStatsMapper;
    private final FlashcardReviewLogMapper reviewLogMapper;

    private static final int DEFAULT_NEW_CARD_LIMIT = 20;
    
    /**
     * 每轮学习的卡片数量
     */
    private static final int CARDS_PER_SESSION = 10;
    
    /**
     * 当前学习会话已学习数量（线程局部变量简化处理，实际生产可用Redis）
     */
    private final ThreadLocal<Integer> sessionStudiedCount = ThreadLocal.withInitial(() -> 0);

    @Override
    public TodayStudyResponse getTodayStudy(Long userId) {
        LocalDate today = LocalDate.now();
        TodayStudyResponse response = new TodayStudyResponse();

        // 待复习数量
        int reviewCount = studyRecordMapper.countDueReviews(userId, today);
        response.setReviewCount(reviewCount);

        // 新卡数量（限制每日数量）
        List<Flashcard> newCards = flashcardMapper.selectNewCards(userId, null, DEFAULT_NEW_CARD_LIMIT);
        response.setNewCount(newCards.size());

        // 今日总任务
        response.setTotalToday(reviewCount + newCards.size());

        // 今日已完成
        FlashcardDailyStats todayStats = dailyStatsMapper.selectByUserAndDate(userId, today);
        int completed = 0;
        if (todayStats != null) {
            completed = todayStats.getNewCount() + todayStats.getReviewCount();
        }
        response.setCompletedToday(completed);

        return response;
    }

    @Override
    public StudyCardResponse startStudy(Long userId, StudyStartRequest request) {
        initTodayStats(userId);
        // 重置本轮学习计数
        sessionStudiedCount.set(0);
        return getNextCard(userId, request.getDeckId(), request.getMode());
    }

    @Override
    public StudyCardResponse getNextCard(Long userId, Long deckId, String mode) {
        // 检查本轮是否已完成10张
        int studied = sessionStudiedCount.get();
        if (studied >= CARDS_PER_SESSION) {
            return null; // 本轮学习完成
        }
        
        LocalDate today = LocalDate.now();
        Flashcard card = null;
        boolean isNew = false;

        // 优先获取待复习的卡
        List<FlashcardStudyRecord> dueReviews = studyRecordMapper.selectDueReviews(userId, deckId, today, 1);
        if (!dueReviews.isEmpty()) {
            FlashcardStudyRecord record = dueReviews.get(0);
            card = flashcardMapper.selectById(record.getCardId());
        }

        // 如果没有待复习的，获取新卡
        if (card == null) {
            List<Flashcard> newCards = flashcardMapper.selectNewCards(userId, deckId, 1);
            if (!newCards.isEmpty()) {
                card = newCards.get(0);
                isNew = true;
            }
        }

        if (card == null) {
            return null; // 没有可学习的卡片
        }

        // 构建响应
        StudyCardResponse response = new StudyCardResponse();
        response.setCardId(card.getId());
        response.setFrontContent(card.getFrontContent());
        response.setBackContent(card.getBackContent());
        response.setDeckId(card.getDeckId());
        response.setDeckName(card.getDeckName());
        response.setIsNew(isNew);

        // 获取复习次数
        FlashcardStudyRecord record = studyRecordMapper.selectByUserAndCard(userId, card.getId());
        response.setReviewCount(record != null ? record.getTotalReviews() : 0);

        // 本轮进度（当前第几张 / 总共 10 张）
        response.setCurrent(studied + 1);
        response.setTotal(CARDS_PER_SESSION);

        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewResultResponse submitReview(Long userId, ReviewSubmitRequest request) {
        Long cardId = request.getCardId();
        Integer quality = request.getQuality();

        // 验证评分范围
        if (quality < 0 || quality > 3) {
            throw new BusinessException("评分必须在0-3之间");
        }

        // 获取闪卡
        Flashcard card = flashcardMapper.selectById(cardId);
        if (card == null) {
            throw new BusinessException("闪卡不存在");
        }

        // 获取或创建学习记录
        FlashcardStudyRecord record = studyRecordMapper.selectByUserAndCard(userId, cardId);
        boolean isNew = (record == null);

        if (isNew) {
            record = new FlashcardStudyRecord()
                    .setUserId(userId)
                    .setCardId(cardId)
                    .setDeckId(card.getDeckId())
                    .setEaseFactor(SM2Algorithm.getDefaultEaseFactor())
                    .setIntervalDays(0)
                    .setRepetitions(0)
                    .setTotalReviews(0)
                    .setCorrectCount(0)
                    .setWrongCount(0)
                    .setStatus(FlashcardStudyRecord.STATUS_NEW)
                    .setCreateTime(LocalDateTime.now());
        }

        // 记录复习前的状态
        var efBefore = record.getEaseFactor();
        var intervalBefore = record.getIntervalDays();

        // 使用SM-2算法计算
        SM2Result result = SM2Algorithm.calculate(
                quality,
                record.getRepetitions(),
                record.getEaseFactor(),
                record.getIntervalDays()
        );

        // 更新学习记录
        record.setEaseFactor(result.getEaseFactor())
              .setIntervalDays(result.getIntervalDays())
              .setRepetitions(result.getRepetitions())
              .setQuality(quality)
              .setLastReviewTime(LocalDateTime.now())
              .setTotalReviews(record.getTotalReviews() + 1)
              .setStatus(result.getNewStatus())
              .setUpdateTime(LocalDateTime.now());

        // 计算下次复习日期
        LocalDate nextReviewDate;
        if (result.getNeedImmediateReview() != null && result.getNeedImmediateReview()) {
            nextReviewDate = LocalDate.now(); // 今天继续复习
        } else {
            nextReviewDate = LocalDate.now().plusDays(result.getIntervalDays());
        }
        record.setNextReviewDate(nextReviewDate);

        // 更新正确/错误计数
        if (quality >= 2) {
            record.setCorrectCount(record.getCorrectCount() + 1);
        } else {
            record.setWrongCount(record.getWrongCount() + 1);
        }

        // 保存学习记录
        if (isNew) {
            studyRecordMapper.insert(record);
        } else {
            studyRecordMapper.updateById(record);
        }
        
        // 更新本轮学习计数
        sessionStudiedCount.set(sessionStudiedCount.get() + 1);

        // 记录复习日志
        FlashcardReviewLog reviewLog = new FlashcardReviewLog()
                .setUserId(userId)
                .setCardId(cardId)
                .setQuality(quality)
                .setEaseFactorBefore(efBefore)
                .setEaseFactorAfter(result.getEaseFactor())
                .setIntervalBefore(intervalBefore)
                .setIntervalAfter(result.getIntervalDays())
                .setReviewTime(LocalDateTime.now())
                .setTimeSpentMs(request.getTimeSpentMs());
        reviewLogMapper.insert(reviewLog);

        // 更新每日统计
        updateDailyStats(userId, isNew, quality);

        // 构建响应
        ReviewResultResponse response = new ReviewResultResponse();
        response.setNextReviewDate(nextReviewDate);
        response.setIntervalDays(result.getIntervalDays());
        response.setNewStatus(result.getNewStatus());

        // 检查是否还有下一张
        TodayStudyResponse todayStudy = getTodayStudy(userId);
        response.setHasNext(todayStudy.getTotalToday() > todayStudy.getCompletedToday());
        response.setRemainingToday(todayStudy.getTotalToday() - todayStudy.getCompletedToday());

        log.info("提交复习反馈: userId={}, cardId={}, quality={}, nextReview={}", 
                userId, cardId, quality, nextReviewDate);

        return response;
    }

    @Override
    public void initTodayStats(Long userId) {
        LocalDate today = LocalDate.now();
        FlashcardDailyStats stats = dailyStatsMapper.selectByUserAndDate(userId, today);
        if (stats == null) {
            stats = new FlashcardDailyStats()
                    .setUserId(userId)
                    .setStudyDate(today)
                    .setNewCount(0)
                    .setReviewCount(0)
                    .setCorrectCount(0)
                    .setWrongCount(0)
                    .setStudyTimeSeconds(0)
                    .setCreateTime(LocalDateTime.now());
            dailyStatsMapper.insert(stats);
        }
    }

    private void updateDailyStats(Long userId, boolean isNew, int quality) {
        LocalDate today = LocalDate.now();
        FlashcardDailyStats stats = new FlashcardDailyStats()
                .setUserId(userId)
                .setStudyDate(today)
                .setNewCount(isNew ? 1 : 0)
                .setReviewCount(isNew ? 0 : 1)
                .setCorrectCount(quality >= 2 ? 1 : 0)
                .setWrongCount(quality < 2 ? 1 : 0)
                .setStudyTimeSeconds(0)
                .setCreateTime(LocalDateTime.now());
        dailyStatsMapper.upsert(stats);
    }
}
