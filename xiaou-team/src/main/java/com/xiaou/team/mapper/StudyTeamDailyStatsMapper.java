package com.xiaou.team.mapper;

import com.xiaou.team.domain.StudyTeamDailyStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 每日统计Mapper
 * 
 * @author xiaou
 */
@Mapper
public interface StudyTeamDailyStatsMapper {
    
    /**
     * 插入或更新统计
     */
    int insertOrUpdate(StudyTeamDailyStats stats);
    
    /**
     * 根据小组ID和日期查询
     */
    StudyTeamDailyStats selectByTeamIdAndDate(@Param("teamId") Long teamId, @Param("statsDate") LocalDate statsDate);
    
    /**
     * 查询日期范围内的统计
     * 
     * @param teamId 小组ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 统计列表
     */
    List<StudyTeamDailyStats> selectStatsByDateRange(@Param("teamId") Long teamId,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);
    
    /**
     * 统计某日打卡人数
     * 
     * @param teamId 小组ID
     * @param date 日期
     * @return 打卡人数
     */
    Integer countCheckinsByDate(@Param("teamId") Long teamId, @Param("date") LocalDate date);
    
    /**
     * 统计累计打卡次数
     * 
     * @param teamId 小组ID
     * @return 累计打卡次数
     */
    Integer countTotalCheckins(@Param("teamId") Long teamId);
    
    /**
     * 统计累计打卡天数
     * 
     * @param teamId 小组ID
     * @return 累计打卡天数
     */
    Integer countTotalCheckinDays(@Param("teamId") Long teamId);
}
