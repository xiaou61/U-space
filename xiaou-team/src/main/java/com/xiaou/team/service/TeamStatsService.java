package com.xiaou.team.service;

import com.xiaou.team.dto.TeamStatsResponse;

/**
 * 统计服务接口
 * 
 * @author xiaou
 */
public interface TeamStatsService {
    
    /**
     * 获取小组统计概览
     * 
     * @param teamId 小组ID
     * @param userId 当前用户ID（用于获取个人统计）
     * @return 统计数据
     */
    TeamStatsResponse getTeamStats(Long teamId, Long userId);
    
    /**
     * 获取小组每周统计
     * 
     * @param teamId 小组ID
     * @return 每周统计数据
     */
    TeamStatsResponse getWeeklyStats(Long teamId);
    
    /**
     * 获取小组每月统计
     * 
     * @param teamId 小组ID
     * @param year 年
     * @param month 月
     * @return 每月统计数据
     */
    TeamStatsResponse getMonthlyStats(Long teamId, Integer year, Integer month);
    
    /**
     * 获取用户个人统计
     * 
     * @param teamId 小组ID
     * @param userId 用户ID
     * @return 用户统计数据
     */
    TeamStatsResponse.UserStats getUserStats(Long teamId, Long userId);
}
