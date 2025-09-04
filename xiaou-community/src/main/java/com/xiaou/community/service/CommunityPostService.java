package com.xiaou.community.service;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.community.domain.CommunityPost;
import com.xiaou.community.dto.AdminPostQueryRequest;
import com.xiaou.community.dto.CommunityPostQueryRequest;
import com.xiaou.community.dto.CommunityPostCreateRequest;
import com.xiaou.community.dto.CommunityPostResponse;

import java.util.List;

/**
 * 社区帖子Service接口
 * 
 * @author xiaou
 */
public interface CommunityPostService {
    
    /**
     * 后台查询帖子列表
     */
    PageResult<CommunityPost> getAdminPostList(AdminPostQueryRequest request);
    
    /**
     * 根据ID查询帖子
     */
    CommunityPost getById(Long id);
    
    /**
     * 置顶帖子
     */
    void topPost(Long id, Integer duration);
    
    /**
     * 取消置顶
     */
    void cancelTop(Long id);
    
    /**
     * 下架帖子
     */
    void disablePost(Long id);
    
    /**
     * 删除帖子
     */
    void deletePost(Long id);
    
    /**
     * 根据用户ID查询帖子列表
     */
    List<CommunityPost> getPostsByUserId(Long userId);
    
    // 前台接口方法
    
    /**
     * 前台查询帖子列表
     */
    PageResult<CommunityPostResponse> getPostList(CommunityPostQueryRequest request);
    
    /**
     * 前台查询帖子详情
     */
    CommunityPostResponse getPostDetail(Long id);
    
    /**
     * 创建帖子
     */
    void createPost(CommunityPostCreateRequest request);
    
    /**
     * 点赞帖子
     */
    void likePost(Long id);
    
    /**
     * 取消点赞帖子
     */
    void unlikePost(Long id);
    
    /**
     * 收藏帖子
     */
    void collectPost(Long id);
    
    /**
     * 取消收藏帖子
     */
    void uncollectPost(Long id);
    
    /**
     * 获取用户收藏的帖子列表
     */
    PageResult<CommunityPostResponse> getUserCollections(CommunityPostQueryRequest request);
    
    /**
     * 获取用户发布的帖子列表
     */
    PageResult<CommunityPostResponse> getUserPosts(CommunityPostQueryRequest request);
} 