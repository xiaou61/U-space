package com.xiaou.interview.service.impl;

import com.xiaou.interview.domain.InterviewDailyStats;
import com.xiaou.interview.domain.InterviewMasteryRecord;
import com.xiaou.interview.dto.HeatmapResponse;
import com.xiaou.interview.dto.MasteryMarkRequest;
import com.xiaou.interview.dto.MasteryResponse;
import com.xiaou.interview.dto.ReviewStatsResponse;
import com.xiaou.interview.mapper.InterviewDailyStatsMapper;
import com.xiaou.interview.mapper.InterviewMasteryMapper;
import com.xiaou.interview.service.InterviewMasteryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 面试题掌握度服务实现类
 *
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewMasteryServiceImpl implements InterviewMasteryService {

    private final InterviewMasteryMapper masteryMapper;
    private final InterviewDailyStatsMapper dailyStatsMapper;

    /**
     * 基础复习间隔（天）：不会=1, 模糊=2, 熟悉=4, 已掌握=7
     */
    private static final int[] BASE_INTERVALS = {1, 2, 4, 7};

    /**
     * 最大复习间隔（天）
     */
    private static final int MAX_INTERVAL = 60;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MasteryResponse markMastery(Long userId, MasteryMarkRequest request) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();

        // 查询是否已有记录
        InterviewMasteryRecord existingRecord = masteryMapper.selectByUserAndQuestion(userId, request.getQuestionId());

        boolean isReview = existingRecord != null;
        int reviewCount = isReview ? existingRecord.getReviewCount() + 1 : 0;

        // 计算下次复习时间
        int nextReviewDays = calculateNextReviewDays(request.getMasteryLevel(), reviewCount);
        LocalDateTime nextReviewTime = now.plusDays(nextReviewDays);

        InterviewMasteryRecord record;
        if (existingRecord == null) {
            // 新建记录
            record = new InterviewMasteryRecord()
                    .setUserId(userId)
                    .setQuestionId(request.getQuestionId())
                    .setQuestionSetId(request.getQuestionSetId())
                    .setMasteryLevel(request.getMasteryLevel())
                    .setReviewCount(0)
                    .setLastReviewTime(now)
                    .setNextReviewTime(nextReviewTime)
                    .setCreateTime(now)
                    .setUpdateTime(now);
            masteryMapper.insert(record);

            // 更新每日学习统计
            dailyStatsMapper.incrementLearnCount(userId, today);
        } else {
            // 更新记录
            record = existingRecord;
            record.setMasteryLevel(request.getMasteryLevel())
                    .setReviewCount(reviewCount)
                    .setLastReviewTime(now)
                    .setNextReviewTime(nextReviewTime)
                    .setUpdateTime(now);
            masteryMapper.update(record);

            // 更新每日复习统计
            dailyStatsMapper.incrementReviewCount(userId, today);
        }

        // 记录历史
        masteryMapper.insertHistory(userId, request.getQuestionId(), request.getMasteryLevel(), isReview ? 1 : 0);

        log.info("用户{}标记题目{}掌握度为{}, 下次复习时间: {}", userId, request.getQuestionId(), 
                request.getMasteryLevel(), nextReviewTime);

        return buildMasteryResponse(record, nextReviewDays);
    }

    @Override
    public MasteryResponse getMastery(Long userId, Long questionId) {
        InterviewMasteryRecord record = masteryMapper.selectByUserAndQuestion(userId, questionId);
        if (record == null) {
            return null;
        }
        return buildMasteryResponse(record, null);
    }

    @Override
    public Map<Long, MasteryResponse> batchGetMastery(Long userId, List<Long> questionIds) {
        if (questionIds == null || questionIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<InterviewMasteryRecord> records = masteryMapper.selectByUserAndQuestions(userId, questionIds);
        Map<Long, MasteryResponse> result = new HashMap<>();

        for (InterviewMasteryRecord record : records) {
            result.put(record.getQuestionId(), buildMasteryResponse(record, null));
        }

        return result;
    }

    @Override
    public ReviewStatsResponse getReviewStats(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();

        // 今日时间范围
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.plusDays(1).atStartOfDay();

        // 本周时间范围（从今天到7天后）
        LocalDateTime weekEnd = today.plusDays(7).atStartOfDay();

        int overdueCount = masteryMapper.countOverdueReview(userId, now);
        int todayCount = masteryMapper.countTodayReview(userId, todayStart, todayEnd);
        int weekCount = masteryMapper.countWeekReview(userId, todayStart, weekEnd);
        int totalLearned = masteryMapper.countTotalLearned(userId);

        // 各掌握度等级统计
        int level1Count = masteryMapper.countByMasteryLevel(userId, 1);
        int level2Count = masteryMapper.countByMasteryLevel(userId, 2);
        int level3Count = masteryMapper.countByMasteryLevel(userId, 3);
        int level4Count = masteryMapper.countByMasteryLevel(userId, 4);

        return new ReviewStatsResponse()
                .setOverdueCount(overdueCount)
                .setTodayCount(todayCount)
                .setWeekCount(weekCount)
                .setTotalLearned(totalLearned)
                .setLevel1Count(level1Count)
                .setLevel2Count(level2Count)
                .setLevel3Count(level3Count)
                .setLevel4Count(level4Count);
    }

    @Override
    public List<InterviewMasteryRecord> getReviewList(Long userId, String type) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.plusDays(1).atStartOfDay();
        LocalDateTime weekEnd = today.plusDays(7).atStartOfDay();

        switch (type.toLowerCase()) {
            case "overdue":
                return masteryMapper.selectOverdueReview(userId, now);
            case "today":
                return masteryMapper.selectTodayReview(userId, todayStart, todayEnd);
            case "week":
                return masteryMapper.selectWeekReview(userId, todayStart, weekEnd);
            case "all":
            default:
                // 合并逾期和本周
                List<InterviewMasteryRecord> result = new ArrayList<>();
                result.addAll(masteryMapper.selectOverdueReview(userId, now));
                result.addAll(masteryMapper.selectWeekReview(userId, todayStart, weekEnd));
                return result;
        }
    }

    @Override
    public HeatmapResponse getHeatmap(Long userId, int year) {
        // 获取该年的每日统计数据
        List<InterviewDailyStats> yearStats = dailyStatsMapper.selectByUserAndYear(userId, year);

        // 转换为每日数据列表
        List<HeatmapResponse.DailyData> dailyDataList = yearStats.stream()
                .map(stats -> new HeatmapResponse.DailyData()
                        .setDate(stats.getStatDate())
                        .setCount(stats.getTotalCount())
                        .setLevel(HeatmapResponse.calculateLevel(stats.getTotalCount()))
                        .setLearnCount(stats.getLearnCount())
                        .setReviewCount(stats.getReviewCount()))
                .collect(Collectors.toList());

        // 统计总学习天数
        int totalDays = dailyStatsMapper.countTotalDays(userId);

        // 计算当前连续学习天数
        int currentStreak = calculateCurrentStreak(userId, LocalDate.now());

        // 计算最长连续学习天数
        int longestStreak = calculateLongestStreak(yearStats);

        // 计算各月学习天数
        Map<String, Integer> monthStats = new LinkedHashMap<>();
        for (int month = 1; month <= 12; month++) {
            int count = dailyStatsMapper.countMonthDays(userId, year, month);
            monthStats.put(String.format("%d-%02d", year, month), count);
        }

        return new HeatmapResponse()
                .setYear(year)
                .setTotalDays(totalDays)
                .setCurrentStreak(currentStreak)
                .setLongestStreak(longestStreak)
                .setMonthStats(monthStats)
                .setDailyData(dailyDataList);
    }

    @Override
    public Map<String, Object> getDayDetail(Long userId, String date) {
        LocalDate localDate = LocalDate.parse(date);
        InterviewDailyStats stats = dailyStatsMapper.selectByUserAndDate(userId, localDate);

        Map<String, Object> result = new HashMap<>();
        result.put("date", date);

        if (stats == null) {
            result.put("learnCount", 0);
            result.put("reviewCount", 0);
            result.put("totalCount", 0);
        } else {
            result.put("learnCount", stats.getLearnCount());
            result.put("reviewCount", stats.getReviewCount());
            result.put("totalCount", stats.getTotalCount());
        }

        return result;
    }

    /**
     * 计算下次复习时间间隔（天）
     * 基于艾宾浩斯遗忘曲线
     */
    private int calculateNextReviewDays(int masteryLevel, int reviewCount) {
        // 基础间隔
        int baseInterval = BASE_INTERVALS[masteryLevel - 1];
        // 复习次数越多，间隔越长（指数增长，但有上限）
        double multiplier = Math.pow(2, Math.min(reviewCount, 5));
        int days = (int) (baseInterval * multiplier);
        return Math.min(days, MAX_INTERVAL);
    }

    /**
     * 构建掌握度响应
     */
    private MasteryResponse buildMasteryResponse(InterviewMasteryRecord record, Integer nextReviewDays) {
        LocalDateTime now = LocalDateTime.now();
        boolean isOverdue = record.getNextReviewTime() != null && record.getNextReviewTime().isBefore(now);

        if (nextReviewDays == null && record.getNextReviewTime() != null) {
            nextReviewDays = (int) ChronoUnit.DAYS.between(LocalDate.now(), record.getNextReviewTime().toLocalDate());
        }

        return new MasteryResponse()
                .setQuestionId(record.getQuestionId())
                .setMasteryLevel(record.getMasteryLevel())
                .setMasteryLevelText(MasteryResponse.getMasteryLevelText(record.getMasteryLevel()))
                .setReviewCount(record.getReviewCount())
                .setLastReviewTime(record.getLastReviewTime())
                .setNextReviewTime(record.getNextReviewTime())
                .setNextReviewDays(nextReviewDays)
                .setIsOverdue(isOverdue);
    }

    /**
     * 计算当前连续学习天数
     */
    private int calculateCurrentStreak(Long userId, LocalDate today) {
        int streak = 0;
        LocalDate checkDate = today;

        while (true) {
            InterviewDailyStats stats = dailyStatsMapper.selectByUserAndDate(userId, checkDate);
            if (stats != null && stats.getTotalCount() > 0) {
                streak++;
                checkDate = checkDate.minusDays(1);
            } else {
                break;
            }
        }

        return streak;
    }

    /**
     * 计算最长连续学习天数
     */
    private int calculateLongestStreak(List<InterviewDailyStats> stats) {
        if (stats == null || stats.isEmpty()) {
            return 0;
        }

        int longestStreak = 0;
        int currentStreak = 0;
        LocalDate lastDate = null;

        for (InterviewDailyStats stat : stats) {
            if (stat.getTotalCount() > 0) {
                if (lastDate == null || stat.getStatDate().equals(lastDate.plusDays(1))) {
                    currentStreak++;
                } else {
                    currentStreak = 1;
                }
                lastDate = stat.getStatDate();
                longestStreak = Math.max(longestStreak, currentStreak);
            } else {
                currentStreak = 0;
                lastDate = null;
            }
        }

        return longestStreak;
    }
}
