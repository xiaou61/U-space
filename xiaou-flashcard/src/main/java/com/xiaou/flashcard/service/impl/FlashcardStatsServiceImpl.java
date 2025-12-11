package com.xiaou.flashcard.service.impl;

import com.xiaou.flashcard.domain.FlashcardDailyStats;
import com.xiaou.flashcard.dto.response.StatsOverviewResponse;
import com.xiaou.flashcard.mapper.FlashcardDailyStatsMapper;
import com.xiaou.flashcard.mapper.FlashcardMapper;
import com.xiaou.flashcard.mapper.FlashcardStudyRecordMapper;
import com.xiaou.flashcard.service.FlashcardStatsService;
import com.xiaou.flashcard.service.FlashcardStudyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 闪卡统计服务实现类
 *
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FlashcardStatsServiceImpl implements FlashcardStatsService {

    private final FlashcardMapper flashcardMapper;
    private final FlashcardStudyRecordMapper studyRecordMapper;
    private final FlashcardDailyStatsMapper dailyStatsMapper;
    private final FlashcardStudyService studyService;

    @Override
    public StatsOverviewResponse getOverview(Long userId) {
        StatsOverviewResponse response = new StatsOverviewResponse();

        // 闪卡总数
        int totalCards = flashcardMapper.countByUserId(userId);
        response.setTotalCards(totalCards);

        // 已掌握数量
        int masteredCards = studyRecordMapper.countMastered(userId);
        response.setMasteredCards(masteredCards);

        // 学习中数量
        int learningCards = studyRecordMapper.countLearning(userId);
        response.setLearningCards(learningCards);

        // 新卡数量
        response.setNewCards(totalCards - masteredCards - learningCards);

        // 连续学习天数
        int streakDays = getStreakDays(userId);
        response.setStreakDays(streakDays);

        // 总复习次数
        int totalReviews = studyRecordMapper.sumTotalReviews(userId);
        response.setTotalReviews(totalReviews);

        // 今日数据
        var todayStudy = studyService.getTodayStudy(userId);
        response.setTodayReviewCount(todayStudy.getReviewCount());
        response.setTodayNewCount(todayStudy.getNewCount());
        response.setTodayCompletedCount(todayStudy.getCompletedToday());

        return response;
    }

    @Override
    public List<FlashcardDailyStats> getDailyTrend(Long userId, Integer days) {
        if (days == null || days <= 0) {
            days = 7;
        }
        return dailyStatsMapper.selectRecentDays(userId, days);
    }

    @Override
    public int getStreakDays(Long userId) {
        // 简化实现：计算从今天往前连续学习的天数
        List<FlashcardDailyStats> recentStats = dailyStatsMapper.selectRecentDays(userId, 365);
        if (recentStats.isEmpty()) {
            return 0;
        }

        int streak = 0;
        LocalDate expectedDate = LocalDate.now();

        for (FlashcardDailyStats stats : recentStats) {
            if (stats.getStudyDate().equals(expectedDate)) {
                if (stats.getNewCount() + stats.getReviewCount() > 0) {
                    streak++;
                    expectedDate = expectedDate.minusDays(1);
                } else {
                    break;
                }
            } else if (stats.getStudyDate().isBefore(expectedDate)) {
                // 中间有缺失的日期，连续中断
                break;
            }
        }

        return streak;
    }
}
