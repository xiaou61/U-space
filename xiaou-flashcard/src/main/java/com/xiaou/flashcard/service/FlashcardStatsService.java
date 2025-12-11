package com.xiaou.flashcard.service;

import com.xiaou.flashcard.domain.FlashcardDailyStats;
import com.xiaou.flashcard.dto.response.StatsOverviewResponse;

import java.util.List;

/**
 * 闪卡统计服务接口
 *
 * @author xiaou
 */
public interface FlashcardStatsService {

    /**
     * 获取学习统计概览
     */
    StatsOverviewResponse getOverview(Long userId);

    /**
     * 获取每日学习趋势
     */
    List<FlashcardDailyStats> getDailyTrend(Long userId, Integer days);

    /**
     * 获取连续学习天数
     */
    int getStreakDays(Long userId);
}
