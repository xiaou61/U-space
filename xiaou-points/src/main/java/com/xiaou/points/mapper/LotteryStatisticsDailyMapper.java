package com.xiaou.points.mapper;

import com.xiaou.points.domain.LotteryStatisticsDaily;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 抽奖每日统计Mapper
 * 
 * @author xiaou
 */
@Mapper
public interface LotteryStatisticsDailyMapper {
    
    /**
     * 查询今日统计
     */
    LotteryStatisticsDaily selectToday();
    
    /**
     * 插入统计记录
     */
    int insert(LotteryStatisticsDaily statistics);
    
    /**
     * 增加今日抽奖次数
     */
    int incrementTodayDraw();
    
    /**
     * 增加今日中奖次数
     */
    int incrementTodayWin();
    
    /**
     * 查询历史统计（日期范围）
     */
    List<LotteryStatisticsDaily> selectByDateRange(@Param("startDate") LocalDate startDate, 
                                                     @Param("endDate") LocalDate endDate);
    
    /**
     * 查询最近N天统计
     */
    List<LotteryStatisticsDaily> selectRecentDays(@Param("days") Integer days);
}

