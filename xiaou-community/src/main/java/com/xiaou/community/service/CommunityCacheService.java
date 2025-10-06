package com.xiaou.community.service;

import com.xiaou.community.domain.CommunityCategory;
import com.xiaou.community.domain.CommunityPost;
import com.xiaou.community.domain.CommunityTag;

import java.util.List;
import java.util.Set;

/**
 * 社区缓存Service
 * 
 * @author xiaou
 */
public interface CommunityCacheService {
    
    /**
     * 缓存帖子详情
     */
    void cachePost(CommunityPost post);
    
    /**
     * 获取缓存的帖子详情
     */
    CommunityPost getCachedPost(Long postId);
    
    /**
     * 删除帖子缓存
     */
    void evictPost(Long postId);
    
    /**
     * 缓存分类列表
     */
    void cacheCategories(List<CommunityCategory> categories);
    
    /**
     * 获取缓存的分类列表
     */
    List<CommunityCategory> getCachedCategories();
    
    /**
     * 删除分类缓存
     */
    void evictCategories();
    
    /**
     * 缓存标签列表
     */
    void cacheTags(List<CommunityTag> tags);
    
    /**
     * 获取缓存的标签列表
     */
    List<CommunityTag> getCachedTags();
    
    /**
     * 删除标签缓存
     */
    void evictTags();
    
    /**
     * 缓存用户点赞状态
     */
    void cacheUserLikeStatus(Long userId, Set<Long> likedPostIds);
    
    /**
     * 获取用户点赞状态
     */
    Set<Long> getUserLikedPostIds(Long userId);
    
    /**
     * 添加用户点赞的帖子
     */
    void addUserLikedPost(Long userId, Long postId);
    
    /**
     * 移除用户点赞的帖子
     */
    void removeUserLikedPost(Long userId, Long postId);
    
    /**
     * 缓存用户收藏状态
     */
    void cacheUserCollectStatus(Long userId, Set<Long> collectedPostIds);
    
    /**
     * 获取用户收藏状态
     */
    Set<Long> getUserCollectedPostIds(Long userId);
    
    /**
     * 添加用户收藏的帖子
     */
    void addUserCollectedPost(Long userId, Long postId);
    
    /**
     * 移除用户收藏的帖子
     */
    void removeUserCollectedPost(Long userId, Long postId);
    
    /**
     * 记录搜索关键词（增加搜索次数）
     */
    void recordSearchKeyword(String keyword);
    
    /**
     * 获取热门搜索词列表
     */
    List<String> getHotSearchKeywords(Integer limit);
    
    /**
     * 清除热门搜索词缓存
     */
    void clearHotSearchKeywords();
}

