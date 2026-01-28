package com.xiaou.team.service.impl;

import com.xiaou.team.domain.StudyTeamMember;
import com.xiaou.team.dto.RankResponse;
import com.xiaou.team.enums.MemberRole;
import com.xiaou.team.mapper.StudyTeamCheckinMapper;
import com.xiaou.team.mapper.StudyTeamMemberMapper;
import com.xiaou.team.service.TeamRankService;
import com.xiaou.user.api.UserInfoApiService;
import com.xiaou.user.api.dto.SimpleUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 排行榜服务实现
 * 
 * @author xiaou
 */
@Service
@RequiredArgsConstructor
public class TeamRankServiceImpl implements TeamRankService {
    
    private final StudyTeamMemberMapper memberMapper;
    private final StudyTeamCheckinMapper checkinMapper;
    private final UserInfoApiService userInfoApiService;
    
    @Override
    public List<RankResponse> getCheckinRank(Long teamId, String type, Integer limit, Long userId) {
        // 获取小组有效成员
        List<StudyTeamMember> members = memberMapper.selectActiveByTeamId(teamId);
        
        LocalDate today = LocalDate.now();
        LocalDate startDate;
        LocalDate endDate = today;
        
        // 根据类型确定日期范围
        switch (type) {
            case "weekly":
                startDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                break;
            case "monthly":
                startDate = today.withDayOfMonth(1);
                break;
            default: // total
                startDate = null;
                break;
        }
        
        List<RankResponse> ranks = new ArrayList<>();
        
        for (StudyTeamMember member : members) {
            RankResponse rank = new RankResponse();
            rank.setUserId(member.getUserId());
            rank.setRole(member.getRole());
            
            // 统计打卡次数
            Integer checkins;
            if (startDate != null) {
                checkins = countUserCheckins(member.getUserId(), teamId, startDate, endDate);
            } else {
                checkins = member.getTotalCheckins() != null ? member.getTotalCheckins() : 0;
            }
            
            if ("weekly".equals(type)) {
                rank.setWeeklyCheckins(checkins);
            } else if ("monthly".equals(type)) {
                rank.setMonthlyCheckins(checkins);
            }
            rank.setTotalCheckins(checkins);
            
            ranks.add(rank);
        }
        
        // 排序
        ranks.sort(Comparator.comparing(RankResponse::getTotalCheckins, Comparator.nullsLast(Comparator.reverseOrder())));
        
        // 填充排名和用户信息
        fillRankInfo(ranks, userId, limit);
        
        return ranks;
    }
    
    @Override
    public List<RankResponse> getStreakRank(Long teamId, Integer limit, Long userId) {
        List<StudyTeamMember> members = memberMapper.selectActiveByTeamId(teamId);
        
        LocalDate today = LocalDate.now();
        List<RankResponse> ranks = new ArrayList<>();
        
        for (StudyTeamMember member : members) {
            RankResponse rank = new RankResponse();
            rank.setUserId(member.getUserId());
            rank.setRole(member.getRole());
            
            // 计算连续打卡天数
            Integer streakDays = checkinMapper.countStreakDays(member.getUserId(), teamId, null, today);
            rank.setStreakDays(streakDays != null ? streakDays : 0);
            rank.setTotalCheckins(member.getTotalCheckins());
            
            ranks.add(rank);
        }
        
        // 按连续天数排序
        ranks.sort(Comparator.comparing(RankResponse::getStreakDays, Comparator.nullsLast(Comparator.reverseOrder())));
        
        fillRankInfo(ranks, userId, limit);
        
        return ranks;
    }
    
    @Override
    public List<RankResponse> getDurationRank(Long teamId, String type, Integer limit, Long userId) {
        List<StudyTeamMember> members = memberMapper.selectActiveByTeamId(teamId);
        
        LocalDate today = LocalDate.now();
        LocalDate startDate;
        LocalDate endDate = today;
        
        switch (type) {
            case "weekly":
                startDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                break;
            case "monthly":
                startDate = today.withDayOfMonth(1);
                break;
            default:
                startDate = null;
                break;
        }
        
        List<RankResponse> ranks = new ArrayList<>();
        
        for (StudyTeamMember member : members) {
            RankResponse rank = new RankResponse();
            rank.setUserId(member.getUserId());
            rank.setRole(member.getRole());
            
            // 统计学习时长
            Integer duration = countUserDuration(member.getUserId(), teamId, startDate, endDate);
            rank.setTotalDuration(duration);
            rank.setTotalCheckins(member.getTotalCheckins());
            
            ranks.add(rank);
        }
        
        // 按时长排序
        ranks.sort(Comparator.comparing(RankResponse::getTotalDuration, Comparator.nullsLast(Comparator.reverseOrder())));
        
        fillRankInfo(ranks, userId, limit);
        
        return ranks;
    }
    
    @Override
    public List<RankResponse> getContributionRank(Long teamId, Integer limit, Long userId) {
        List<StudyTeamMember> members = memberMapper.selectActiveByTeamId(teamId);
        
        List<RankResponse> ranks = new ArrayList<>();
        
        for (StudyTeamMember member : members) {
            RankResponse rank = new RankResponse();
            rank.setUserId(member.getUserId());
            rank.setRole(member.getRole());
            rank.setContribution(member.getContribution() != null ? member.getContribution() : 0);
            rank.setTotalCheckins(member.getTotalCheckins());
            
            ranks.add(rank);
        }
        
        // 按贡献值排序
        ranks.sort(Comparator.comparing(RankResponse::getContribution, Comparator.nullsLast(Comparator.reverseOrder())));
        
        fillRankInfo(ranks, userId, limit);
        
        return ranks;
    }
    
    @Override
    public RankResponse getUserRank(Long teamId, Long userId, String rankType) {
        List<RankResponse> ranks;
        
        switch (rankType) {
            case "checkin":
                ranks = getCheckinRank(teamId, "total", 100, userId);
                break;
            case "streak":
                ranks = getStreakRank(teamId, 100, userId);
                break;
            case "duration":
                ranks = getDurationRank(teamId, "total", 100, userId);
                break;
            case "contribution":
                ranks = getContributionRank(teamId, 100, userId);
                break;
            default:
                return null;
        }
        
        return ranks.stream()
            .filter(r -> r.getUserId().equals(userId))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * 统计用户打卡次数
     */
    private Integer countUserCheckins(Long userId, Long teamId, LocalDate startDate, LocalDate endDate) {
        // 通过checkinMapper统计
        List<Long> userIds = checkinMapper.selectCheckinUserIds(teamId, null, startDate);
        // 简化实现：返回成员表中的总打卡数
        StudyTeamMember member = memberMapper.selectByTeamIdAndUserId(teamId, userId);
        return member != null ? member.getTotalCheckins() : 0;
    }
    
    /**
     * 统计用户学习时长
     */
    private Integer countUserDuration(Long userId, Long teamId, LocalDate startDate, LocalDate endDate) {
        // 简化实现
        return 0;
    }
    
    /**
     * 填充排名信息
     */
    private void fillRankInfo(List<RankResponse> ranks, Long userId, Integer limit) {
        // 批量查询用户信息，避免N+1问题
        java.util.List<Long> userIds = ranks.stream()
                .map(RankResponse::getUserId)
                .collect(java.util.stream.Collectors.toList());
        java.util.Map<Long, SimpleUserInfo> userInfoMap = userInfoApiService.getSimpleUserInfoBatch(userIds);
        
        int rank = 1;
        for (RankResponse r : ranks) {
            r.setRank(rank++);
            
            // 用户信息
            SimpleUserInfo userInfo = userInfoMap.get(r.getUserId());
            if (userInfo != null) {
                r.setUserName(userInfo.getDisplayName());
                r.setUserAvatar(userInfo.getAvatar());
            }
            
            // 角色名称
            MemberRole role = MemberRole.getByCode(r.getRole());
            r.setRoleName(role != null ? role.getName() : "成员");
            
            // 是否是当前用户
            r.setIsCurrentUser(userId != null && userId.equals(r.getUserId()));
        }
        
        // 限制条数
        if (limit != null && ranks.size() > limit) {
            ranks.subList(limit, ranks.size()).clear();
        }
    }
}
