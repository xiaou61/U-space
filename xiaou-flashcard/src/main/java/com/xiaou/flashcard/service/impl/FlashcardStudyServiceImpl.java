package com.xiaou.flashcard.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.xiaou.common.exception.BusinessException;
import com.xiaou.flashcard.algorithm.SM2Algorithm;
import com.xiaou.flashcard.domain.Flashcard;
import com.xiaou.flashcard.domain.FlashcardDailyStats;
import com.xiaou.flashcard.domain.FlashcardDeck;
import com.xiaou.flashcard.domain.FlashcardStudyRecord;
import com.xiaou.flashcard.dto.request.FlashcardStudySubmitRequest;
import com.xiaou.flashcard.dto.response.FlashcardHeatmapVO;
import com.xiaou.flashcard.dto.response.FlashcardStudyResultVO;
import com.xiaou.flashcard.dto.response.FlashcardStudyStatsVO;
import com.xiaou.flashcard.dto.response.FlashcardStudyVO;
import com.xiaou.flashcard.enums.MasteryLevel;
import com.xiaou.flashcard.mapper.FlashcardDailyStatsMapper;
import com.xiaou.flashcard.mapper.FlashcardDeckMapper;
import com.xiaou.flashcard.mapper.FlashcardMapper;
import com.xiaou.flashcard.mapper.FlashcardStudyRecordMapper;
import com.xiaou.flashcard.service.FlashcardStudyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 闪卡学习服务实现
 *
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlashcardStudyServiceImpl implements FlashcardStudyService {

    private final FlashcardMapper cardMapper;
    private final FlashcardDeckMapper deckMapper;
    private final FlashcardStudyRecordMapper studyRecordMapper;
    private final FlashcardDailyStatsMapper dailyStatsMapper;

    /**
     * 每日新卡上限
     */
    private static final int DAILY_NEW_CARD_LIMIT = 20;

    @Override
    public List<FlashcardStudyVO> getTodayStudyCards(Long userId, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 20;
        }

        List<FlashcardStudyVO> result = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        // 1. 获取待复习的卡片（优先）
        List<FlashcardStudyRecord> dueRecords = studyRecordMapper.selectDueCards(userId, null, now, limit);
        for (FlashcardStudyRecord record : dueRecords) {
            Flashcard card = cardMapper.selectById(record.getCardId());
            if (card != null) {
                FlashcardDeck deck = deckMapper.selectById(card.getDeckId());
                result.add(convertToStudyVO(card, deck, record));
            }
        }

        // 2. 如果还有空位，补充新卡片
        if (result.size() < limit) {
            int remaining = limit - result.size();
            // 获取用户的所有卡组
            List<FlashcardDeck> decks = deckMapper.selectByUserId(userId);
            Set<Long> addedCardIds = result.stream()
                    .map(FlashcardStudyVO::getId)
                    .collect(Collectors.toSet());

            for (FlashcardDeck deck : decks) {
                if (remaining <= 0) break;

                // 获取该卡组中未学习的卡片
                List<Long> learnedIds = studyRecordMapper.selectLearnedCardIds(userId, deck.getId());
                Set<Long> learnedSet = new HashSet<>(learnedIds);
                List<Long> allCardIds = cardMapper.selectIdsByDeckId(deck.getId());

                for (Long cardId : allCardIds) {
                    if (remaining <= 0) break;
                    if (learnedSet.contains(cardId) || addedCardIds.contains(cardId)) {
                        continue;
                    }
                    Flashcard card = cardMapper.selectById(cardId);
                    if (card != null) {
                        result.add(convertToStudyVO(card, deck, null));
                        addedCardIds.add(cardId);
                        remaining--;
                    }
                }
            }
        }

        return result;
    }

    @Override
    public FlashcardStudyVO getNextCard(Long deckId, Long userId) {
        FlashcardDeck deck = deckMapper.selectById(deckId);
        if (deck == null) {
            throw new BusinessException("卡组不存在");
        }

        LocalDateTime now = LocalDateTime.now();

        // 1. 优先返回待复习的卡片
        List<FlashcardStudyRecord> dueRecords = studyRecordMapper.selectDueCards(userId, deckId, now, 1);
        if (!dueRecords.isEmpty()) {
            FlashcardStudyRecord record = dueRecords.get(0);
            Flashcard card = cardMapper.selectById(record.getCardId());
            if (card != null) {
                return convertToStudyVO(card, deck, record);
            }
        }

        // 2. 返回新卡片
        List<Long> learnedIds = studyRecordMapper.selectLearnedCardIds(userId, deckId);
        Set<Long> learnedSet = new HashSet<>(learnedIds);
        List<Long> allCardIds = cardMapper.selectIdsByDeckId(deckId);

        for (Long cardId : allCardIds) {
            if (!learnedSet.contains(cardId)) {
                Flashcard card = cardMapper.selectById(cardId);
                if (card != null) {
                    return convertToStudyVO(card, deck, null);
                }
            }
        }

        return null; // 没有待学习的卡片
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FlashcardStudyResultVO submitStudyResult(FlashcardStudySubmitRequest request, Long userId) {
        Flashcard card = cardMapper.selectById(request.getCardId());
        if (card == null) {
            throw new BusinessException("闪卡不存在");
        }

        LocalDateTime now = LocalDateTime.now();
        FlashcardStudyRecord record = studyRecordMapper.selectByUserAndCard(userId, request.getCardId());

        // 将1-4的评分映射到SM-2的0-5
        int sm2Quality = mapQualityToSM2(request.getQuality());
        boolean isCorrect = sm2Quality >= 3;
        boolean isNew = (record == null);

        if (record == null) {
            // 新卡片，首次学习
            record = new FlashcardStudyRecord();
            record.setUserId(userId);
            record.setCardId(request.getCardId());
            record.setDeckId(card.getDeckId());
            record.setRepetitions(0);
            record.setEaseFactor(2.5);
            record.setIntervalDays(0);
            record.setMasteryLevel(MasteryLevel.NEW.getCode());
            record.setTotalReviews(0);
            record.setCorrectCount(0);
            record.setCreateTime(now);

            // 增加卡组学习人数（首次学习该卡组的卡片）
            int learnedCount = studyRecordMapper.countLearnedCards(userId, card.getDeckId());
            if (learnedCount == 0) {
                deckMapper.incrementStudyCount(card.getDeckId());
            }
        }

        // 使用SM-2算法计算
        SM2Algorithm.SM2Result sm2Result = SM2Algorithm.calculate(
                record.getRepetitions(),
                record.getEaseFactor(),
                record.getIntervalDays(),
                sm2Quality
        );

        // 更新记录
        record.setRepetitions(sm2Result.getRepetitions());
        record.setEaseFactor(sm2Result.getEaseFactor());
        record.setIntervalDays(sm2Result.getIntervalDays());
        record.setMasteryLevel(sm2Result.getMasteryLevel());
        record.setLastReviewTime(now);
        record.setNextReviewTime(now.plusDays(sm2Result.getIntervalDays()));
        record.setTotalReviews(record.getTotalReviews() + 1);
        if (isCorrect) {
            record.setCorrectCount(record.getCorrectCount() + 1);
        }
        record.setUpdateTime(now);

        if (isNew) {
            studyRecordMapper.insert(record);
        } else {
            studyRecordMapper.update(record);
        }

        // 更新每日统计
        dailyStatsMapper.incrementStats(
                userId,
                LocalDate.now(),
                isNew ? 1 : 0,
                isNew ? 0 : 1,
                isCorrect ? 1 : 0,
                request.getDuration() != null ? request.getDuration() : 0
        );

        // 构建返回结果
        FlashcardStudyResultVO result = new FlashcardStudyResultVO();
        result.setCardId(request.getCardId());
        result.setMasteryLevel(record.getMasteryLevel());
        result.setNextReviewTime(record.getNextReviewTime());
        result.setIntervalDays(record.getIntervalDays());
        result.setRemainingDue(studyRecordMapper.countDueCards(userId, null, now));

        return result;
    }

    @Override
    public FlashcardStudyStatsVO getStudyStats(Long userId, Long deckId) {
        FlashcardStudyStatsVO stats = new FlashcardStudyStatsVO();
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();

        // 待复习数
        stats.setTodayDueCount(studyRecordMapper.countDueCards(userId, deckId, now));

        // 已学习卡片数
        stats.setTotalLearnedCount(studyRecordMapper.countLearnedCards(userId, deckId));

        // 各掌握程度的数量
        stats.setNewCount(studyRecordMapper.countByMasteryLevel(userId, deckId, MasteryLevel.NEW.getCode()));
        stats.setLearningCount(studyRecordMapper.countByMasteryLevel(userId, deckId, MasteryLevel.LEARNING.getCode()));
        stats.setMasteredCount(studyRecordMapper.countByMasteryLevel(userId, deckId, MasteryLevel.MASTERED.getCode()));

        // 今日统计
        FlashcardDailyStats todayStats = dailyStatsMapper.selectByUserAndDate(userId, today);
        if (todayStats != null) {
            stats.setTodayLearnedCount(todayStats.getNewCards() + todayStats.getReviewCards());
            stats.setTodayDuration(todayStats.getStudyDuration() / 60); // 转换为分钟
        } else {
            stats.setTodayLearnedCount(0);
            stats.setTodayDuration(0);
        }

        // 计算新卡数
        if (deckId != null) {
            int totalCards = cardMapper.countByDeckId(deckId);
            stats.setTodayNewCount(Math.min(DAILY_NEW_CARD_LIMIT, totalCards - stats.getTotalLearnedCount()));
        } else {
            stats.setTodayNewCount(DAILY_NEW_CARD_LIMIT);
        }

        // 学习天数统计
        stats.setTotalStudyDays(dailyStatsMapper.countStudyDays(userId));
        stats.setStreakDays(calculateStreakDays(userId, today));

        return stats;
    }

    /**
     * 计算连续学习天数
     */
    private int calculateStreakDays(Long userId, LocalDate endDate) {
        // 获取最近365天的学习日期
        List<LocalDate> recentDates = dailyStatsMapper.selectRecentDates(userId, endDate, 365);
        if (recentDates == null || recentDates.isEmpty()) {
            return 0;
        }

        int streak = 0;
        LocalDate checkDate = endDate;

        for (LocalDate date : recentDates) {
            if (date.equals(checkDate)) {
                streak++;
                checkDate = checkDate.minusDays(1);
            } else if (date.isBefore(checkDate)) {
                // 如果第一天不是今天，但是是昨天，也算连续
                if (streak == 0 && date.equals(endDate.minusDays(1))) {
                    streak++;
                    checkDate = date.minusDays(1);
                } else {
                    break;
                }
            }
        }

        return streak;
    }

    @Override
    public FlashcardHeatmapVO getHeatmap(Long userId, Integer days) {
        if (days == null || days <= 0) {
            days = 365;
        }

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        List<FlashcardDailyStats> statsList = dailyStatsMapper.selectByUserAndDateRange(userId, startDate, endDate);

        // 转换为map方便查找
        java.util.Map<LocalDate, FlashcardDailyStats> statsMap = statsList.stream()
                .collect(java.util.stream.Collectors.toMap(FlashcardDailyStats::getStatDate, s -> s));

        // 生成完整日期范围的数据
        List<FlashcardHeatmapVO.DailyData> data = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            FlashcardHeatmapVO.DailyData dailyData = new FlashcardHeatmapVO.DailyData();
            dailyData.setDate(date);

            FlashcardDailyStats stats = statsMap.get(date);
            if (stats != null) {
                int count = stats.getNewCards() + stats.getReviewCards();
                dailyData.setCount(count);
                dailyData.setDuration(stats.getStudyDuration() / 60);
                dailyData.setLevel(calculateHeatLevel(count));
            } else {
                dailyData.setCount(0);
                dailyData.setDuration(0);
                dailyData.setLevel(0);
            }

            data.add(dailyData);
        }

        FlashcardHeatmapVO result = new FlashcardHeatmapVO();
        result.setData(data);
        return result;
    }

    /**
     * 将用户评分(1-4)映射到SM-2评分(0-5)
     */
    private int mapQualityToSM2(int quality) {
        switch (quality) {
            case 1: return 1; // 完全忘记
            case 2: return 3; // 模糊记忆
            case 3: return 4; // 想起来了
            case 4: return 5; // 完全记住
            default: return 3;
        }
    }

    /**
     * 计算热度等级
     */
    private int calculateHeatLevel(int count) {
        if (count == 0) return 0;
        if (count <= 5) return 1;
        if (count <= 15) return 2;
        if (count <= 30) return 3;
        return 4;
    }

    private FlashcardStudyVO convertToStudyVO(Flashcard card, FlashcardDeck deck, FlashcardStudyRecord record) {
        FlashcardStudyVO vo = new FlashcardStudyVO();
        vo.setId(card.getId());
        vo.setDeckId(card.getDeckId());
        vo.setDeckName(deck != null ? deck.getName() : null);
        vo.setFrontContent(card.getFrontContent());
        vo.setBackContent(card.getBackContent());
        vo.setContentType(card.getContentType());
        vo.setTags(card.getTags());

        if (record != null) {
            vo.setIsNew(false);
            vo.setMasteryLevel(record.getMasteryLevel());
            vo.setLastReviewTime(record.getLastReviewTime());
            vo.setTotalReviews(record.getTotalReviews());
        } else {
            vo.setIsNew(true);
            vo.setMasteryLevel(MasteryLevel.NEW.getCode());
            vo.setTotalReviews(0);
        }

        return vo;
    }
}
