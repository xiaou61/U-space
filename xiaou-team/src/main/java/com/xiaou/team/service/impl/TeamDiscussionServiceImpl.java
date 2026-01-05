package com.xiaou.team.service.impl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaou.team.domain.StudyTeamDiscussion;
import com.xiaou.team.domain.StudyTeamMember;
import com.xiaou.team.dto.DiscussionCreateRequest;
import com.xiaou.team.dto.DiscussionResponse;
import com.xiaou.team.enums.DiscussionCategory;
import com.xiaou.team.enums.MemberRole;
import com.xiaou.team.mapper.StudyTeamDiscussionMapper;
import com.xiaou.team.mapper.StudyTeamMemberMapper;
import com.xiaou.team.service.TeamDiscussionService;
import com.xiaou.user.api.UserInfoApiService;
import com.xiaou.user.api.dto.SimpleUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 讨论服务实现
 * 
 * @author xiaou
 */
@Service
@RequiredArgsConstructor
public class TeamDiscussionServiceImpl implements TeamDiscussionService {
    
    private final StudyTeamDiscussionMapper discussionMapper;
    private final StudyTeamMemberMapper memberMapper;
    private final UserInfoApiService userInfoApiService;
    private final ObjectMapper objectMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createDiscussion(Long teamId, DiscussionCreateRequest request, Long userId) {
        // 验证用户是小组成员
        StudyTeamMember member = memberMapper.selectByTeamIdAndUserId(teamId, userId);
        if (member == null || member.getStatus() != 1) {
            throw new RuntimeException("您不是小组成员");
        }
        
        // 检查是否被禁言
        if (member.getMuteEndTime() != null && member.getMuteEndTime().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("您已被禁言，无法发布讨论");
        }
        
        // 公告类只有管理员以上可以发
        if (request.getCategory() != null && request.getCategory() == DiscussionCategory.ANNOUNCEMENT.getCode()) {
            if (member.getRole() != MemberRole.LEADER.getCode() && member.getRole() != MemberRole.ADMIN.getCode()) {
                throw new RuntimeException("只有组长或管理员可以发布公告");
            }
        }
        
        StudyTeamDiscussion discussion = new StudyTeamDiscussion();
        discussion.setTeamId(teamId);
        discussion.setUserId(userId);
        discussion.setCategory(request.getCategory() != null ? request.getCategory() : DiscussionCategory.CHAT.getCode());
        discussion.setTitle(request.getTitle());
        discussion.setContent(request.getContent());
        
        if (!CollectionUtils.isEmpty(request.getImages())) {
            try {
                discussion.setImages(objectMapper.writeValueAsString(request.getImages()));
            } catch (JsonProcessingException e) {
                discussion.setImages("[]");
            }
        }
        
        discussion.setViewCount(0);
        discussion.setLikeCount(0);
        discussion.setCommentCount(0);
        discussion.setIsTop(request.getIsTop() != null ? request.getIsTop() : 0);
        discussion.setIsEssence(request.getIsEssence() != null ? request.getIsEssence() : 0);
        discussion.setCreateBy(userId);
        discussion.setCreateTime(LocalDateTime.now());
        discussion.setIsDeleted(0);
        
        discussionMapper.insert(discussion);
        return discussion.getId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateDiscussion(Long discussionId, DiscussionCreateRequest request, Long userId) {
        StudyTeamDiscussion discussion = discussionMapper.selectById(discussionId);
        if (discussion == null || discussion.getIsDeleted() == 1) {
            throw new RuntimeException("讨论不存在");
        }
        
        // 只有作者可以编辑
        if (!discussion.getUserId().equals(userId)) {
            throw new RuntimeException("只能编辑自己的讨论");
        }
        
        discussion.setTitle(request.getTitle());
        discussion.setContent(request.getContent());
        if (!CollectionUtils.isEmpty(request.getImages())) {
            try {
                discussion.setImages(objectMapper.writeValueAsString(request.getImages()));
            } catch (JsonProcessingException e) {
                discussion.setImages("[]");
            }
        }
        discussion.setUpdateBy(userId);
        discussion.setUpdateTime(LocalDateTime.now());
        
        discussionMapper.update(discussion);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDiscussion(Long discussionId, Long userId) {
        StudyTeamDiscussion discussion = discussionMapper.selectById(discussionId);
        if (discussion == null || discussion.getIsDeleted() == 1) {
            throw new RuntimeException("讨论不存在");
        }
        
        // 作者或管理员可以删除
        boolean canDelete = discussion.getUserId().equals(userId);
        if (!canDelete) {
            StudyTeamMember member = memberMapper.selectByTeamIdAndUserId(discussion.getTeamId(), userId);
            if (member != null && member.getStatus() == 1 && (member.getRole() == MemberRole.LEADER.getCode() || 
                                   member.getRole() == MemberRole.ADMIN.getCode())) {
                canDelete = true;
            }
        }
        
        if (!canDelete) {
            throw new RuntimeException("您没有权限删除此讨论");
        }
        
        discussionMapper.deleteById(discussionId);
    }
    
    @Override
    public DiscussionResponse getDiscussionDetail(Long discussionId, Long userId) {
        DiscussionResponse discussion = discussionMapper.selectDiscussionById(discussionId);
        if (discussion == null) {
            throw new RuntimeException("讨论不存在");
        }
        
        // 增加浏览量
        discussionMapper.incrementViewCount(discussionId);
        discussion.setViewCount(discussion.getViewCount() + 1);
        
        fillDiscussionExtraInfo(discussion, userId);
        return discussion;
    }
    
    @Override
    public List<DiscussionResponse> getDiscussionList(Long teamId, Integer category, String keyword,
                                                       Integer page, Integer pageSize, Long userId) {
        int offset = (page - 1) * pageSize;
        List<DiscussionResponse> discussions = discussionMapper.selectDiscussionList(teamId, category, keyword, pageSize, offset);
        
        for (DiscussionResponse discussion : discussions) {
            fillDiscussionExtraInfo(discussion, userId);
        }
        
        return discussions;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setTop(Long discussionId, Integer isTop, Long userId) {
        StudyTeamDiscussion discussion = discussionMapper.selectById(discussionId);
        if (discussion == null || discussion.getIsDeleted() == 1) {
            throw new RuntimeException("讨论不存在");
        }
        
        // 检查权限
        checkAdminPermission(discussion.getTeamId(), userId);
        
        discussionMapper.updateTopStatus(discussionId, isTop);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setEssence(Long discussionId, Integer isEssence, Long userId) {
        StudyTeamDiscussion discussion = discussionMapper.selectById(discussionId);
        if (discussion == null || discussion.getIsDeleted() == 1) {
            throw new RuntimeException("讨论不存在");
        }
        
        // 检查权限
        checkAdminPermission(discussion.getTeamId(), userId);
        
        discussionMapper.updateEssenceStatus(discussionId, isEssence);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void likeDiscussion(Long discussionId, Long userId) {
        StudyTeamDiscussion discussion = discussionMapper.selectById(discussionId);
        if (discussion == null || discussion.getIsDeleted() == 1) {
            throw new RuntimeException("讨论不存在");
        }
        
        // 简化实现：直接增加点赞数
        discussionMapper.updateLikeCount(discussionId, 1);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlikeDiscussion(Long discussionId, Long userId) {
        StudyTeamDiscussion discussion = discussionMapper.selectById(discussionId);
        if (discussion == null || discussion.getIsDeleted() == 1) {
            throw new RuntimeException("讨论不存在");
        }
        
        discussionMapper.updateLikeCount(discussionId, -1);
    }
    
    /**
     * 填充讨论额外信息
     */
    private void fillDiscussionExtraInfo(DiscussionResponse discussion, Long userId) {
        // 解析图片JSON
        if (discussion.getImagesJson() != null && !discussion.getImagesJson().isEmpty()) {
            try {
                discussion.setImages(objectMapper.readValue(discussion.getImagesJson(), 
                    objectMapper.getTypeFactory().constructCollectionType(java.util.List.class, String.class)));
            } catch (Exception e) {
                discussion.setImages(java.util.Collections.emptyList());
            }
        }
        
        // 用户信息
        if (discussion.getUserId() != null) {
            SimpleUserInfo userInfo = userInfoApiService.getSimpleUserInfo(discussion.getUserId());
            if (userInfo != null) {
                discussion.setUserName(userInfo.getDisplayName());
                discussion.setUserAvatar(userInfo.getAvatar());
            }
            
            // 获取用户在小组中的角色
            StudyTeamMember member = memberMapper.selectByTeamIdAndUserId(discussion.getTeamId(), discussion.getUserId());
            if (member != null) {
                discussion.setUserRole(member.getRole());
                MemberRole role = MemberRole.getByCode(member.getRole());
                discussion.setUserRoleName(role != null ? role.getName() : "成员");
            }
        }
        
        // 分类名称
        DiscussionCategory category = DiscussionCategory.getByCode(discussion.getCategory());
        discussion.setCategoryName(category != null ? category.getName() : "其他");
        
        // 是否是作者
        discussion.setIsOwner(userId != null && userId.equals(discussion.getUserId()));
        
        // 相对时间
        discussion.setRelativeTime(getRelativeTime(discussion.getCreateTime()));
        
        // 简化：点赞状态设为false
        discussion.setLiked(false);
    }
    
    /**
     * 检查管理员权限
     */
    private void checkAdminPermission(Long teamId, Long userId) {
        StudyTeamMember member = memberMapper.selectByTeamIdAndUserId(teamId, userId);
        
        if (member == null || member.getStatus() != 1) {
            throw new RuntimeException("您不是小组成员");
        }
        
        if (member.getRole() != MemberRole.LEADER.getCode() && 
            member.getRole() != MemberRole.ADMIN.getCode()) {
            throw new RuntimeException("您没有权限执行此操作");
        }
    }
    
    /**
     * 获取相对时间
     */
    private String getRelativeTime(LocalDateTime time) {
        if (time == null) {
            return "";
        }
        
        Duration duration = Duration.between(time, LocalDateTime.now());
        long minutes = duration.toMinutes();
        
        if (minutes < 1) {
            return "刚刚";
        } else if (minutes < 60) {
            return minutes + "分钟前";
        } else if (minutes < 1440) {
            return (minutes / 60) + "小时前";
        } else if (minutes < 10080) {
            return (minutes / 1440) + "天前";
        } else {
            return time.toLocalDate().toString();
        }
    }
}
