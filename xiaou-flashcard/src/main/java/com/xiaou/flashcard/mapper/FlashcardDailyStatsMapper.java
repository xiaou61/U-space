package com.xiaou.flashcard.mapper;

import com.xiaou.flashcard.domain.FlashcardDailyStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 闪卡每日学习统计 Mapper 接口
 *
 * @author xiaou
 */
@Mapper
public interface FlashcardDailyStatsMapper {

    /**
     * 查询用户某天的统计
     */
    FlashcardDailyStats selectByUserAndDate(@Param("userId") Long userId,
                                            @Param("statDate") LocalDate statDate);

    /**
     * 查询用户一段时间内的统计（用于热力图）
     */
    List<FlashcardDailyStats> selectByUserAndDateRange(@Param("userId") Long userId,
                                                       @Param("startDate") LocalDate startDate,
                                                       @Param("endDate") LocalDate endDate);

    /**
     * 插入统计记录
     */
    int insert(FlashcardDailyStats stats);

    /**
     * 更新统计记录（增量更新）
     */
    int incrementStats(@Param("userId") Long userId,
                       @Param("statDate") LocalDate statDate,
                       @Param("newCards") int newCards,
                       @Param("reviewCards") int reviewCards,
                       @Param("correctCards") int correctCards,
                       @Param("duration") int duration);

    /**
     * 统计用户总学习天数
     */
    int countStudyDays(@Param("userId") Long userId);

    /**
     * 获取用户最近的学习日期列表（用于计算连续学习天数）
     */
    List<LocalDate> selectRecentDates(@Param("userId") Long userId,
                                       @Param("endDate") LocalDate endDate,
                                       @Param("limit") int limit);
}
