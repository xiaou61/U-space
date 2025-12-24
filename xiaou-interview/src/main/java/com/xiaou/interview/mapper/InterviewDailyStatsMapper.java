package com.xiaou.interview.mapper;

import com.xiaou.interview.domain.InterviewDailyStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 面试题每日学习统计Mapper
 *
 * @author xiaou
 */
@Mapper
public interface InterviewDailyStatsMapper {

    /**
     * 插入或更新每日统计
     */
    int upsert(InterviewDailyStats stats);

    /**
     * 查询用户某天的统计
     */
    InterviewDailyStats selectByUserAndDate(@Param("userId") Long userId, @Param("statDate") LocalDate statDate);

    /**
     * 查询用户日期范围内的统计数据（用于热力图）
     */
    List<InterviewDailyStats> selectByUserAndDateRange(@Param("userId") Long userId, 
                                                        @Param("startDate") LocalDate startDate, 
                                                        @Param("endDate") LocalDate endDate);

    /**
     * 查询用户某年的统计数据
     */
    List<InterviewDailyStats> selectByUserAndYear(@Param("userId") Long userId, @Param("year") int year);

    /**
     * 统计用户总学习天数
     */
    int countTotalDays(@Param("userId") Long userId);

    /**
     * 查询用户当前连续学习天数
     */
    int countCurrentStreak(@Param("userId") Long userId, @Param("today") LocalDate today);

    /**
     * 统计用户某月的学习天数
     */
    int countMonthDays(@Param("userId") Long userId, @Param("year") int year, @Param("month") int month);

    /**
     * 增加学习数量
     */
    int incrementLearnCount(@Param("userId") Long userId, @Param("statDate") LocalDate statDate);

    /**
     * 增加复习数量
     */
    int incrementReviewCount(@Param("userId") Long userId, @Param("statDate") LocalDate statDate);
}
