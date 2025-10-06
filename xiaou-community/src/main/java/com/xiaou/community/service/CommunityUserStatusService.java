package com.xiaou.community.service;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.community.domain.CommunityUserStatus;
import com.xiaou.community.dto.AdminUserQueryRequest;

/**
 * 用户社区状态Service接口
 * 
 * @author xiaou
 */
public interface CommunityUserStatusService {
    
    /**
     * 后台查询用户状态列表
     */
    PageResult<CommunityUserStatus> getAdminUserList(AdminUserQueryRequest request);
    
    /**
     * 根据用户ID查询状态
     */
    CommunityUserStatus getByUserId(Long userId);
    
    /**
     * 封禁用户
     */
    void banUser(Long userId, String reason, Integer duration);
    
    /**
     * 解封用户
     */
    void unbanUser(Long userId);
    
    /**
     * 确保用户在社区用户表中存在，如果不存在则创建
     */
    CommunityUserStatus ensureUserExists(Long userId, String userName);
    
    /**
     * 获取当前用户的社区状态，如果不存在则自动创建
     */
    CommunityUserStatus getCurrentUserStatus();
    
    /**
     * 检查当前用户是否被封禁，如果被封禁则抛出异常
     */
    void checkUserBanStatus();
    
    /**
     * 增加用户发帖数
     */
    void incrementPostCount(Long userId);
    
    /**
     * 减少用户发帖数
     */
    void decrementPostCount(Long userId);

    /**
     * 增加用户评论数
     */
    void incrementCommentCount(Long userId);
    
    /**
     * 减少用户评论数
     */
    void decrementCommentCount(Long userId);

    /**
     * 增加用户点赞数
     */
    void incrementLikeCount(Long userId);
    
    /**
     * 减少用户点赞数
     */
    void decrementLikeCount(Long userId);
    
    /**
     * 增加用户收藏数
     */
    void incrementCollectCount(Long userId);
    
    /**
     * 减少用户收藏数
     */
    void decrementCollectCount(Long userId);
    
    /**
     * 根据用户ID获取用户状态，如果不存在则自动创建
     */
    CommunityUserStatus getUserStatusByUserId(Long userId);
} 