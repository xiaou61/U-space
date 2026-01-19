package com.xiaou.team.service.impl;

import com.xiaou.team.domain.StudyTeam;
import com.xiaou.team.domain.StudyTeamMember;
import com.xiaou.team.dto.RankResponse;
import com.xiaou.team.dto.TeamStatsResponse;
import com.xiaou.team.mapper.*;
import com.xiaou.team.service.TeamRankService;
import com.xiaou.team.service.TeamStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

/**
 * 统计服务实现
 * 
 * @author xiaou
 */
@Service
@RequiredArgsConstructor
public class TeamStatsServiceImpl implements TeamStatsService {
    
    private final StudyTeamMapper teamMapper;
    private final StudyTeamMemberMapper memberMapper;
    private final StudyTeamTaskMapper taskMapper;
    private final StudyTeamCheckinMapper checkinMapper;
    private final StudyTeamDiscussionMapper discussionMapper;
    private final StudyTeamDailyStatsMapper dailyStatsMapper;
    private final TeamRankService rankService;
    
    @Override
    public TeamStatsResponse getTeamStats(Long teamId, Long userId) {
        StudyTeam team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new RuntimeException("小组不存在");
        }
        
        TeamStatsResponse stats = new TeamStatsResponse();
        stats.setTeamId(teamId);
        stats.setTeamName(team.getTeamName());
        
        // 成员数
        Integer memberCount = memberMapper.selectCount(teamId).intValue();
        stats.setMemberCount(memberCount);
        
        // 今日打卡人数
        LocalDate today = LocalDate.now();
        Integer todayCheckins = dailyStatsMapper.countCheckinsByDate(teamId, today);
        stats.setTodayCheckinCount(todayCheckins != null ? todayCheckins : 0);
        
        // 今日打卡率
        if (memberCount > 0) {
            stats.setTodayCheckinRate((double) stats.getTodayCheckinCount() / memberCount * 100);
        } else {
            stats.setTodayCheckinRate(0.0);
        }
        
        // 累计打卡次数
        Integer totalCheckins = dailyStatsMapper.countTotalCheckins(teamId);
        stats.setTotalCheckins(totalCheckins != null ? totalCheckins : 0);
        
        // 累计打卡天数
        Integer totalDays = dailyStatsMapper.countTotalCheckinDays(teamId);
        stats.setTotalCheckinDays(totalDays != null ? totalDays : 0);
        
        // 讨论数量
        Integer discussionCount = discussionMapper.countDiscussions(teamId, null);
        stats.setDiscussionCount(discussionCount != null ? discussionCount : 0);
        
        // 任务数量
        Integer taskCount = taskMapper.selectTaskList(teamId, null).size();
        stats.setTaskCount(taskCount);
        
        // 活跃任务数
        Integer activeTaskCount = taskMapper.selectTaskList(teamId, 1).size();
        stats.setActiveTaskCount(activeTaskCount);
        
        // 本周统计
        stats.setWeeklyStats(getWeeklyDailyStats(teamId, memberCount));
        
        // 用户个人统计
        if (userId != null) {
            stats.setUserStats(getUserStats(teamId, userId));
        }
        
        return stats;
    }
    
    @Override
    public TeamStatsResponse getWeeklyStats(Long teamId) {
        StudyTeam team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new RuntimeException("小组不存在");
        }
        
        TeamStatsResponse stats = new TeamStatsResponse();
        stats.setTeamId(teamId);
        stats.setTeamName(team.getTeamName());
        
        Integer memberCount = memberMapper.selectCount(teamId).intValue();
        stats.setMemberCount(memberCount);
        
        stats.setWeeklyStats(getWeeklyDailyStats(teamId, memberCount));
        
        return stats;
    }
    
    @Override
    public TeamStatsResponse getMonthlyStats(Long teamId, Integer year, Integer month) {
        StudyTeam team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new RuntimeException("小组不存在");
        }
        
        TeamStatsResponse stats = new TeamStatsResponse();
        stats.setTeamId(teamId);
        stats.setTeamName(team.getTeamName());
        
        Integer memberCount = memberMapper.selectCount(teamId).intValue();
        stats.setMemberCount(memberCount);
        
        stats.setMonthlyStats(getMonthlyDailyStats(teamId, year, month, memberCount));
        
        return stats;
    }
    
    @Override
    public TeamStatsResponse.UserStats getUserStats(Long teamId, Long userId) {
        StudyTeamMember member = memberMapper.selectByTeamIdAndUserId(teamId, userId);
        
        if (member == null) {
            return null;
        }
        
        TeamStatsResponse.UserStats userStats = new TeamStatsResponse.UserStats();
        userStats.setUserId(userId);
        userStats.setTotalCheckins(member.getTotalCheckins() != null ? member.getTotalCheckins() : 0);
        userStats.setContribution(member.getContribution() != null ? member.getContribution() : 0);
        
        // 连续打卡天数
        LocalDate today = LocalDate.now();
        Integer streakDays = checkinMapper.countStreakDays(userId, teamId, null, today);
        userStats.setStreakDays(streakDays != null ? streakDays : 0);
        
        // 获取排名
        RankResponse rank = rankService.getUserRank(teamId, userId, "checkin");
        if (rank != null) {
            userStats.setRank(rank.getRank());
        }
        
        // 本周/本月打卡数简化实现
        userStats.setWeeklyCheckins(0);
        userStats.setMonthlyCheckins(0);
        userStats.setTotalDuration(0);
        
        return userStats;
    }
    
    /**
     * 获取本周每日统计
     */
    private List<TeamStatsResponse.DailyStats> getWeeklyDailyStats(Long teamId, Integer memberCount) {
        List<TeamStatsResponse.DailyStats> dailyStatsList = new ArrayList<>();
        
        LocalDate today = LocalDate.now();
        LocalDate monday = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        
        for (int i = 0; i < 7; i++) {
            LocalDate date = monday.plusDays(i);
            if (date.isAfter(today)) {
                break;
            }
            
            TeamStatsResponse.DailyStats dailyStats = new TeamStatsResponse.DailyStats();
            dailyStats.setDate(date);
            
            Integer checkinCount = dailyStatsMapper.countCheckinsByDate(teamId, date);
            dailyStats.setCheckinCount(checkinCount != null ? checkinCount : 0);
            
            if (memberCount > 0) {
                dailyStats.setCheckinRate((double) dailyStats.getCheckinCount() / memberCount * 100);
            } else {
                dailyStats.setCheckinRate(0.0);
            }
            
            dailyStats.setTotalDuration(0); // 简化实现
            
            dailyStatsList.add(dailyStats);
        }
        
        return dailyStatsList;
    }
    
    /**
     * 获取月度每日统计
     */
    private List<TeamStatsResponse.DailyStats> getMonthlyDailyStats(Long teamId, Integer year, Integer month, Integer memberCount) {
        List<TeamStatsResponse.DailyStats> dailyStatsList = new ArrayList<>();
        
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        LocalDate today = LocalDate.now();
        
        if (endDate.isAfter(today)) {
            endDate = today;
        }
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            TeamStatsResponse.DailyStats dailyStats = new TeamStatsResponse.DailyStats();
            dailyStats.setDate(date);
            
            Integer checkinCount = dailyStatsMapper.countCheckinsByDate(teamId, date);
            dailyStats.setCheckinCount(checkinCount != null ? checkinCount : 0);
            
            if (memberCount > 0) {
                dailyStats.setCheckinRate((double) dailyStats.getCheckinCount() / memberCount * 100);
            } else {
                dailyStats.setCheckinRate(0.0);
            }
            
            dailyStats.setTotalDuration(0);
            
            dailyStatsList.add(dailyStats);
        }
        
        return dailyStatsList;
    }
}
