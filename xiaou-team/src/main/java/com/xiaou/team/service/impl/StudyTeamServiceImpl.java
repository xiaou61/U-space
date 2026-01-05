package com.xiaou.team.service.impl;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.exception.BusinessException;
import com.xiaou.common.utils.PageHelper;
import com.xiaou.team.domain.StudyTeam;
import com.xiaou.team.domain.StudyTeamMember;
import com.xiaou.team.dto.*;
import com.xiaou.team.enums.*;
import com.xiaou.team.mapper.StudyTeamMapper;
import com.xiaou.team.mapper.StudyTeamMemberMapper;
import com.xiaou.team.service.StudyTeamService;
import com.xiaou.user.api.UserInfoApiService;
import com.xiaou.user.api.dto.SimpleUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学习小组服务实现类
 * 
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudyTeamServiceImpl implements StudyTeamService {
    
    private final StudyTeamMapper teamMapper;
    private final StudyTeamMemberMapper memberMapper;
    private final UserInfoApiService userInfoApiService;
    
    /**
     * 最大创建小组数
     */
    private static final int MAX_CREATE_TEAMS = 3;
    
    /**
     * 最大加入小组数
     */
    private static final int MAX_JOIN_TEAMS = 10;
    
    @Override
    @Transactional
    public TeamResponse createTeam(Long userId, TeamCreateRequest request) {
        // 参数校验
        if (StrUtil.isBlank(request.getTeamName())) {
            throw new BusinessException("小组名称不能为空");
        }
        if (request.getTeamName().length() < 2 || request.getTeamName().length() > 50) {
            throw new BusinessException("小组名称长度需要在2-50个字符之间");
        }
        if (request.getTeamType() == null) {
            throw new BusinessException("小组类型不能为空");
        }
        
        // 检查创建数量限制
        int createdCount = teamMapper.countByCreatorId(userId);
        if (createdCount >= MAX_CREATE_TEAMS) {
            throw new BusinessException("每个用户最多创建" + MAX_CREATE_TEAMS + "个小组");
        }
        
        // 构建小组实体
        StudyTeam team = new StudyTeam();
        team.setTeamName(request.getTeamName());
        team.setTeamDesc(request.getTeamDesc());
        team.setTeamAvatar(request.getTeamAvatar());
        team.setTeamType(request.getTeamType());
        team.setTags(request.getTags());
        team.setMaxMembers(request.getMaxMembers() != null ? request.getMaxMembers() : 20);
        team.setCurrentMembers(1); // 创建者自动成为成员
        team.setJoinType(request.getJoinType() != null ? request.getJoinType() : JoinType.APPLY.getCode());
        team.setInviteCode(generateInviteCode());
        team.setGoalTitle(request.getGoalTitle());
        team.setGoalDesc(request.getGoalDesc());
        team.setGoalStartDate(request.getGoalStartDate());
        team.setGoalEndDate(request.getGoalEndDate());
        team.setDailyTarget(request.getDailyTarget());
        team.setTotalCheckins(0);
        team.setTotalDiscussions(0);
        team.setActiveDays(0);
        team.setCreatorId(userId);
        team.setStatus(TeamStatus.NORMAL.getCode());
        team.setCreateTime(LocalDateTime.now());
        team.setUpdateTime(LocalDateTime.now());
        
        teamMapper.insert(team);
        
        // 创建者自动成为组长
        StudyTeamMember member = new StudyTeamMember();
        member.setTeamId(team.getId());
        member.setUserId(userId);
        member.setRole(MemberRole.LEADER.getCode());
        member.setTotalCheckins(0);
        member.setCurrentStreak(0);
        member.setMaxStreak(0);
        member.setTotalLikesReceived(0);
        member.setContributionPoints(0);
        member.setStatus(MemberStatus.NORMAL.getCode());
        member.setJoinTime(LocalDateTime.now());
        member.setLastActiveTime(LocalDateTime.now());
        memberMapper.insert(member);
        
        log.info("用户{}创建小组成功，小组ID：{}", userId, team.getId());
        
        return convertToTeamResponse(team, userId);
    }
    
    @Override
    @Transactional
    public TeamResponse updateTeam(Long userId, Long teamId, TeamCreateRequest request) {
        StudyTeam team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new BusinessException("小组不存在");
        }
        
        // 检查权限（只有组长可以修改）
        Integer role = memberMapper.selectRole(teamId, userId);
        if (role == null || !role.equals(MemberRole.LEADER.getCode())) {
            throw new BusinessException("只有组长可以修改小组信息");
        }
        
        // 更新字段
        if (StrUtil.isNotBlank(request.getTeamName())) {
            team.setTeamName(request.getTeamName());
        }
        team.setTeamDesc(request.getTeamDesc());
        team.setTeamAvatar(request.getTeamAvatar());
        if (request.getTeamType() != null) {
            team.setTeamType(request.getTeamType());
        }
        team.setTags(request.getTags());
        if (request.getMaxMembers() != null) {
            if (request.getMaxMembers() < team.getCurrentMembers()) {
                throw new BusinessException("最大人数不能小于当前成员数");
            }
            team.setMaxMembers(request.getMaxMembers());
        }
        if (request.getJoinType() != null) {
            team.setJoinType(request.getJoinType());
        }
        team.setGoalTitle(request.getGoalTitle());
        team.setGoalDesc(request.getGoalDesc());
        team.setGoalStartDate(request.getGoalStartDate());
        team.setGoalEndDate(request.getGoalEndDate());
        team.setDailyTarget(request.getDailyTarget());
        
        teamMapper.update(team);
        log.info("用户{}更新小组成功，小组ID：{}", userId, teamId);
        
        return convertToTeamResponse(team, userId);
    }
    
    @Override
    @Transactional
    public boolean dissolveTeam(Long userId, Long teamId) {
        StudyTeam team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new BusinessException("小组不存在");
        }
        
        // 检查权限（只有组长可以解散）
        if (!team.getCreatorId().equals(userId)) {
            throw new BusinessException("只有组长可以解散小组");
        }
        
        // 检查成员数量限制
        if (team.getCurrentMembers() > 10) {
            throw new BusinessException("小组成员超过10人，不可直接解散，请先移除成员");
        }
        
        teamMapper.updateStatus(teamId, TeamStatus.DISSOLVED.getCode());
        log.info("用户{}解散小组成功，小组ID：{}", userId, teamId);
        
        return true;
    }
    
    @Override
    public TeamDetailResponse getTeamDetail(Long userId, Long teamId) {
        StudyTeam team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new BusinessException("小组不存在");
        }
        
        TeamDetailResponse response = new TeamDetailResponse();
        response.setId(team.getId());
        response.setTeamName(team.getTeamName());
        response.setTeamDesc(team.getTeamDesc());
        response.setTeamAvatar(team.getTeamAvatar());
        response.setTeamType(team.getTeamType());
        
        TeamType teamType = TeamType.getByCode(team.getTeamType());
        if (teamType != null) {
            response.setTeamTypeName(teamType.getName());
            response.setTeamTypeIcon(teamType.getIcon());
        }
        
        if (StrUtil.isNotBlank(team.getTags())) {
            response.setTagList(Arrays.asList(team.getTags().split(",")));
        }
        
        response.setMaxMembers(team.getMaxMembers());
        response.setCurrentMembers(team.getCurrentMembers());
        response.setJoinType(team.getJoinType());
        
        JoinType joinType = JoinType.getByCode(team.getJoinType());
        if (joinType != null) {
            response.setJoinTypeName(joinType.getName());
        }
        
        // 目标信息
        response.setGoalTitle(team.getGoalTitle());
        response.setGoalDesc(team.getGoalDesc());
        response.setGoalStartDate(team.getGoalStartDate());
        response.setGoalEndDate(team.getGoalEndDate());
        response.setDailyTarget(team.getDailyTarget());
        
        // 计算目标进度
        if (team.getGoalStartDate() != null && team.getGoalEndDate() != null) {
            LocalDate today = LocalDate.now();
            long totalDays = ChronoUnit.DAYS.between(team.getGoalStartDate(), team.getGoalEndDate());
            long passedDays = ChronoUnit.DAYS.between(team.getGoalStartDate(), today);
            if (totalDays > 0) {
                response.setGoalProgress((int) (passedDays * 100 / totalDays));
            }
            long remainingDays = ChronoUnit.DAYS.between(today, team.getGoalEndDate());
            response.setGoalRemainingDays((int) Math.max(0, remainingDays));
        }
        
        // 统计数据
        response.setTotalCheckins(team.getTotalCheckins());
        response.setTotalDiscussions(team.getTotalDiscussions());
        response.setActiveDays(team.getActiveDays());
        response.setCheckinRate(calculateCheckinRate(teamId));
        
        // 创建者信息
        response.setCreatorId(team.getCreatorId());
        SimpleUserInfo creator = userInfoApiService.getSimpleUserInfo(team.getCreatorId());
        if (creator != null) {
            response.setCreatorName(creator.getDisplayName());
            response.setCreatorAvatar(creator.getAvatar());
        }
        
        response.setStatus(team.getStatus());
        response.setCreateTime(team.getCreateTime());
        
        // 当前用户角色
        if (userId != null) {
            Integer myRole = memberMapper.selectRole(teamId, userId);
            response.setJoined(myRole != null);
            response.setMyRole(myRole);
            if (myRole != null) {
                MemberRole role = MemberRole.getByCode(myRole);
                response.setMyRoleName(role != null ? role.getName() : null);
                // 只有成员才能看到邀请码
                response.setInviteCode(team.getInviteCode());
            }
        }
        
        // 成员头像
        List<String> avatars = memberMapper.selectMemberAvatars(teamId, 5);
        response.setMemberAvatars(avatars);
        
        return response;
    }
    
    @Override
    public PageResult<TeamResponse> getTeamList(Long userId, TeamListRequest request) {
        return PageHelper.doPage(request.getPageNum(), request.getPageSize(), () -> {
            List<StudyTeam> teams = teamMapper.selectList(request);
            return teams.stream()
                    .map(team -> convertToTeamResponse(team, userId))
                    .collect(Collectors.toList());
        });
    }
    
    @Override
    public List<TeamResponse> getMyTeams(Long userId) {
        List<StudyTeam> teams = teamMapper.selectJoinedTeams(userId);
        return teams.stream()
                .map(team -> convertToTeamResponse(team, userId))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TeamResponse> getCreatedTeams(Long userId) {
        List<StudyTeam> teams = teamMapper.selectByCreatorId(userId);
        return teams.stream()
                .map(team -> convertToTeamResponse(team, userId))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TeamResponse> getRecommendTeams(Long userId) {
        List<StudyTeam> teams = teamMapper.selectRecommend(userId, 6);
        return teams.stream()
                .map(team -> convertToTeamResponse(team, userId))
                .collect(Collectors.toList());
    }
    
    @Override
    public String getInviteCode(Long userId, Long teamId) {
        StudyTeam team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new BusinessException("小组不存在");
        }
        
        // 检查是否是成员
        Integer role = memberMapper.selectRole(teamId, userId);
        if (role == null) {
            throw new BusinessException("您不是该小组成员");
        }
        
        return team.getInviteCode();
    }
    
    @Override
    @Transactional
    public String refreshInviteCode(Long userId, Long teamId) {
        StudyTeam team = teamMapper.selectById(teamId);
        if (team == null) {
            throw new BusinessException("小组不存在");
        }
        
        // 检查权限（只有组长和管理员可以刷新）
        Integer role = memberMapper.selectRole(teamId, userId);
        if (role == null || role.equals(MemberRole.MEMBER.getCode())) {
            throw new BusinessException("只有组长或管理员可以刷新邀请码");
        }
        
        String newCode = generateInviteCode();
        teamMapper.updateInviteCode(teamId, newCode);
        log.info("用户{}刷新小组邀请码，小组ID：{}", userId, teamId);
        
        return newCode;
    }
    
    @Override
    public TeamResponse getTeamByInviteCode(String inviteCode) {
        if (StrUtil.isBlank(inviteCode)) {
            throw new BusinessException("邀请码不能为空");
        }
        
        StudyTeam team = teamMapper.selectByInviteCode(inviteCode);
        if (team == null) {
            throw new BusinessException("邀请码无效");
        }
        
        return convertToTeamResponse(team, null);
    }
    
    /**
     * 生成邀请码
     */
    private String generateInviteCode() {
        return RandomUtil.randomString(8).toUpperCase();
    }
    
    /**
     * 计算7日打卡率
     */
    private Integer calculateCheckinRate(Long teamId) {
        // TODO: 实现打卡率计算
        return 0;
    }
    
    /**
     * 转换为响应DTO
     */
    private TeamResponse convertToTeamResponse(StudyTeam team, Long userId) {
        TeamResponse response = new TeamResponse();
        response.setId(team.getId());
        response.setTeamName(team.getTeamName());
        response.setTeamDesc(team.getTeamDesc());
        response.setTeamAvatar(team.getTeamAvatar());
        response.setTeamType(team.getTeamType());
        
        TeamType teamType = TeamType.getByCode(team.getTeamType());
        if (teamType != null) {
            response.setTeamTypeName(teamType.getName());
            response.setTeamTypeIcon(teamType.getIcon());
        }
        
        if (StrUtil.isNotBlank(team.getTags())) {
            response.setTagList(Arrays.asList(team.getTags().split(",")));
        }
        
        response.setMaxMembers(team.getMaxMembers());
        response.setCurrentMembers(team.getCurrentMembers());
        response.setJoinType(team.getJoinType());
        
        JoinType joinType = JoinType.getByCode(team.getJoinType());
        if (joinType != null) {
            response.setJoinTypeName(joinType.getName());
        }
        
        response.setGoalTitle(team.getGoalTitle());
        response.setGoalStartDate(team.getGoalStartDate());
        response.setGoalEndDate(team.getGoalEndDate());
        
        // 计算目标进度
        if (team.getGoalStartDate() != null && team.getGoalEndDate() != null) {
            LocalDate today = LocalDate.now();
            long totalDays = ChronoUnit.DAYS.between(team.getGoalStartDate(), team.getGoalEndDate());
            long passedDays = ChronoUnit.DAYS.between(team.getGoalStartDate(), today);
            if (totalDays > 0) {
                response.setGoalProgress((int) (passedDays * 100 / totalDays));
            }
        }
        
        response.setCheckinRate(calculateCheckinRate(team.getId()));
        response.setCreatorId(team.getCreatorId());
        
        // 创建者信息
        SimpleUserInfo creator = userInfoApiService.getSimpleUserInfo(team.getCreatorId());
        if (creator != null) {
            response.setCreatorName(creator.getDisplayName());
            response.setCreatorAvatar(creator.getAvatar());
        }
        
        response.setStatus(team.getStatus());
        TeamStatus status = TeamStatus.getByCode(team.getStatus());
        if (status != null) {
            response.setStatusName(status.getName());
        }
        
        response.setCreateTime(team.getCreateTime());
        
        // 当前用户角色
        if (userId != null) {
            Integer myRole = memberMapper.selectRole(team.getId(), userId);
            response.setJoined(myRole != null);
            response.setMyRole(myRole);
        }
        
        return response;
    }
}
