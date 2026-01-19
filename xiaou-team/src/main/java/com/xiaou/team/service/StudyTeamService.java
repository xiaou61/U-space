package com.xiaou.team.service;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.team.dto.*;

import java.util.List;

/**
 * 学习小组服务接口
 * 
 * @author xiaou
 */
public interface StudyTeamService {
    
    /**
     * 创建小组
     */
    TeamResponse createTeam(Long userId, TeamCreateRequest request);
    
    /**
     * 更新小组
     */
    TeamResponse updateTeam(Long userId, Long teamId, TeamCreateRequest request);
    
    /**
     * 解散小组
     */
    boolean dissolveTeam(Long userId, Long teamId);
    
    /**
     * 获取小组详情
     */
    TeamDetailResponse getTeamDetail(Long userId, Long teamId);
    
    /**
     * 获取小组列表（广场）
     */
    PageResult<TeamResponse> getTeamList(Long userId, TeamListRequest request);
    
    /**
     * 获取我的小组列表
     */
    List<TeamResponse> getMyTeams(Long userId);
    
    /**
     * 获取我创建的小组列表
     */
    List<TeamResponse> getCreatedTeams(Long userId);
    
    /**
     * 获取推荐小组
     */
    List<TeamResponse> getRecommendTeams(Long userId);
    
    /**
     * 获取邀请码
     */
    String getInviteCode(Long userId, Long teamId);
    
    /**
     * 刷新邀请码
     */
    String refreshInviteCode(Long userId, Long teamId);
    
    /**
     * 根据邀请码获取小组信息
     */
    TeamResponse getTeamByInviteCode(String inviteCode);
}
