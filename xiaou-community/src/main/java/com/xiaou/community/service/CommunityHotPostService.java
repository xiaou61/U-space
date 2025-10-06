package com.xiaou.community.service;

import com.xiaou.community.dto.CommunityPostResponse;

import java.util.List;

/**
 * 社区热门帖子Service
 * 
 * @author xiaou
 */
public interface CommunityHotPostService {
    
    /**
     * 获取热门帖子列表
     */
    List<CommunityPostResponse> getHotPosts(Integer limit);
    
    /**
     * 刷新热门帖子缓存
     */
    void refreshHotPosts();
    
    /**
     * 计算帖子热度分数
     */
    Double calculateHotScore(Long postId);
}

