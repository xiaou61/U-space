package com.xiaou.flashcard.mapper;

import com.xiaou.flashcard.domain.FlashcardDailyStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 闪卡每日统计Mapper
 *
 * @author xiaou
 */
@Mapper
public interface FlashcardDailyStatsMapper {

    /**
     * 插入每日统计
     */
    int insert(FlashcardDailyStats stats);

    /**
     * 更新每日统计
     */
    int updateById(FlashcardDailyStats stats);

    /**
     * 查询用户某天的统计
     */
    FlashcardDailyStats selectByUserAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    /**
     * 查询用户近N天的统计
     */
    List<FlashcardDailyStats> selectRecentDays(@Param("userId") Long userId, @Param("days") Integer days);

    /**
     * 增加新学数量
     */
    int increaseNewCount(@Param("userId") Long userId, @Param("date") LocalDate date);

    /**
     * 增加复习数量
     */
    int increaseReviewCount(@Param("userId") Long userId, @Param("date") LocalDate date);

    /**
     * 增加正确数量
     */
    int increaseCorrectCount(@Param("userId") Long userId, @Param("date") LocalDate date);

    /**
     * 增加错误数量
     */
    int increaseWrongCount(@Param("userId") Long userId, @Param("date") LocalDate date);

    /**
     * 计算连续学习天数
     */
    int countStreakDays(@Param("userId") Long userId);

    /**
     * 插入或更新统计(upsert)
     */
    int upsert(FlashcardDailyStats stats);
}
