package com.xiaou.community.service;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.community.domain.CommunityComment;
import com.xiaou.community.dto.AdminCommentQueryRequest;
import com.xiaou.community.dto.CommunityCommentResponse;
import com.xiaou.community.dto.CommunityCommentCreateRequest;
import com.xiaou.community.dto.CommunityCommentQueryRequest;

import java.util.List;

/**
 * 社区评论Service接口
 * 
 * @author xiaou
 */
public interface CommunityCommentService {
    
    /**
     * 后台查询评论列表
     */
    PageResult<CommunityComment> getAdminCommentList(AdminCommentQueryRequest request);
    
    /**
     * 根据ID查询评论
     */
    CommunityComment getById(Long id);
    
    /**
     * 删除评论
     */
    void deleteComment(Long id);
    
    /**
     * 根据用户ID查询评论列表
     */
    List<CommunityComment> getCommentsByUserId(Long userId);
    
    // 前台接口方法
    
    /**
     * 查询帖子的评论列表
     */
    PageResult<CommunityCommentResponse> getPostComments(Long postId, CommunityCommentQueryRequest request);
    
    /**
     * 发表评论
     */
    void createComment(Long postId, CommunityCommentCreateRequest request);
    
    /**
     * 点赞评论
     */
    void likeComment(Long commentId);
    
    /**
     * 取消点赞评论
     */
    void unlikeComment(Long commentId);
    
    /**
     * 获取用户的评论列表
     */
    PageResult<CommunityCommentResponse> getUserComments(CommunityCommentQueryRequest request);
} 