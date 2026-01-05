package com.xiaou.team.service;

import com.xiaou.team.dto.ApplicationResponse;
import com.xiaou.team.dto.JoinRequest;
import com.xiaou.team.dto.MemberResponse;

import java.util.List;

/**
 * 小组成员服务接口
 * 
 * @author xiaou
 */
public interface TeamMemberService {
    
    /**
     * 申请加入小组
     */
    boolean applyJoin(Long userId, JoinRequest request);
    
    /**
     * 直接加入小组（公开小组或邀请码加入）
     */
    boolean directJoin(Long userId, JoinRequest request);
    
    /**
     * 通过邀请码加入
     */
    boolean joinByInviteCode(Long userId, String inviteCode);
    
    /**
     * 退出小组
     */
    boolean quitTeam(Long userId, Long teamId);
    
    /**
     * 获取小组成员列表
     */
    List<MemberResponse> getMemberList(Long teamId);
    
    /**
     * 审批申请 - 通过
     */
    boolean approveApplication(Long reviewerId, Long applicationId);
    
    /**
     * 审批申请 - 拒绝
     */
    boolean rejectApplication(Long reviewerId, Long applicationId, String rejectReason);
    
    /**
     * 获取小组申请列表
     */
    List<ApplicationResponse> getApplicationList(Long userId, Long teamId);
    
    /**
     * 获取我的申请记录
     */
    List<ApplicationResponse> getMyApplications(Long userId);
    
    /**
     * 取消申请
     */
    boolean cancelApplication(Long userId, Long applicationId);
    
    /**
     * 移除成员
     */
    boolean removeMember(Long operatorId, Long teamId, Long targetUserId);
    
    /**
     * 设置成员角色
     */
    boolean setMemberRole(Long operatorId, Long teamId, Long targetUserId, Integer role);
    
    /**
     * 转让组长
     */
    boolean transferLeader(Long currentLeaderId, Long teamId, Long newLeaderId);
    
    /**
     * 禁言成员
     */
    boolean muteMember(Long operatorId, Long teamId, Long targetUserId, Integer minutes);
    
    /**
     * 解除禁言
     */
    boolean unmuteMember(Long operatorId, Long teamId, Long targetUserId);
    
    /**
     * 获取待审核申请数量
     */
    int getPendingApplicationCount(Long userId, Long teamId);
}
