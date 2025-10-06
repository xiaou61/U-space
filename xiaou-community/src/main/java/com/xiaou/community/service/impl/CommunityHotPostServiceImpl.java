package com.xiaou.community.service.impl;

import com.xiaou.common.utils.RedisUtil;
import com.xiaou.community.config.CommunityProperties;
import com.xiaou.community.domain.CommunityPost;
import com.xiaou.community.dto.CommunityPostResponse;
import com.xiaou.community.mapper.CommunityPostMapper;
import com.xiaou.community.service.CommunityHotPostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 社区热门帖子Service实现类
 * 
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityHotPostServiceImpl implements CommunityHotPostService {
    
    private final CommunityPostMapper communityPostMapper;
    private final CommunityProperties communityProperties;
    private final RedisUtil redisUtil;
    
    private static final String HOT_POSTS_KEY = "community:hot:posts";
    
    @Override
    public List<CommunityPostResponse> getHotPosts(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = communityProperties.getHot().getLimit();
        }
        
        try {
            // 从Redis获取热门帖子ID列表（带分数）
            Map<Object, Double> hotPostMap = redisUtil.zRevRangeWithScores(HOT_POSTS_KEY, 0, limit - 1);
            
            if (hotPostMap == null || hotPostMap.isEmpty()) {
                // 缓存为空，刷新缓存
                log.info("热门帖子缓存为空，开始刷新");
                refreshHotPosts();
                // 重新获取
                hotPostMap = redisUtil.zRevRangeWithScores(HOT_POSTS_KEY, 0, limit - 1);
            }
            
            if (hotPostMap == null || hotPostMap.isEmpty()) {
                log.warn("刷新后仍无热门帖子");
                return new ArrayList<>();
            }
            
            // 根据ID列表查询帖子详情
            List<CommunityPostResponse> hotPosts = new ArrayList<>();
            for (Map.Entry<Object, Double> entry : hotPostMap.entrySet()) {
                Long postId = Long.valueOf(entry.getKey().toString());
                CommunityPost post = communityPostMapper.selectById(postId);
                if (post != null && post.getStatus() == 1) {
                    CommunityPostResponse response = convertToResponse(post);
                    response.setHotScore(entry.getValue());
                    hotPosts.add(response);
                }
            }
            
            return hotPosts;
            
        } catch (Exception e) {
            log.error("获取热门帖子失败", e);
            return new ArrayList<>();
        }
    }
    
    @Override
    public void refreshHotPosts() {
        try {
            log.info("开始刷新热门帖子缓存");
            
            // 计算时间范围
            Integer timeRange = communityProperties.getHot().getTimeRange();
            Date startTime = new Date(System.currentTimeMillis() - timeRange * 60 * 60 * 1000L);
            
            // 查询符合条件的帖子
            List<CommunityPost> posts = communityPostMapper.selectHotPosts(
                startTime, 
                communityProperties.getHot().getMinScore(),
                communityProperties.getHot().getLimit() * 2 // 查询2倍数量，避免过滤后不够
            );
            
            if (posts == null || posts.isEmpty()) {
                log.info("没有符合条件的热门帖子");
                return;
            }
            
            // 清空旧缓存
            redisUtil.del(HOT_POSTS_KEY);
            
            // 计算热度分数并存入Redis
            for (CommunityPost post : posts) {
                Double hotScore = calculateHotScoreInternal(post);
                if (hotScore >= communityProperties.getHot().getMinScore()) {
                    redisUtil.zAdd(HOT_POSTS_KEY, post.getId(), hotScore);
                }
            }
            
            // 保留TOP N
            long size = redisUtil.zSize(HOT_POSTS_KEY);
            if (size > communityProperties.getHot().getLimit()) {
                redisUtil.zRemoveRange(HOT_POSTS_KEY, 0, (int)(size - communityProperties.getHot().getLimit() - 1));
            }
            
            // 设置过期时间
            redisUtil.expire(HOT_POSTS_KEY, communityProperties.getCache().getHotPostsTtl());
            
            log.info("热门帖子缓存刷新完成，共{}个帖子", redisUtil.zSize(HOT_POSTS_KEY));
            
        } catch (Exception e) {
            log.error("刷新热门帖子缓存失败", e);
        }
    }
    
    @Override
    public Double calculateHotScore(Long postId) {
        CommunityPost post = communityPostMapper.selectById(postId);
        if (post == null) {
            return 0.0;
        }
        return calculateHotScoreInternal(post);
    }
    
    /**
     * 计算帖子热度分数
     * 热度分数 = 点赞数 × 3 + 评论数 × 5 + 收藏数 × 8 + 浏览数 × 0.1 - 时间衰减
     * 时间衰减 = (当前时间 - 发布时间) / 24小时 × 10
     */
    private Double calculateHotScoreInternal(CommunityPost post) {
        // 基础分数
        double score = 0.0;
        score += (post.getLikeCount() != null ? post.getLikeCount() : 0) * 3.0;
        score += (post.getCommentCount() != null ? post.getCommentCount() : 0) * 5.0;
        score += (post.getCollectCount() != null ? post.getCollectCount() : 0) * 8.0;
        score += (post.getViewCount() != null ? post.getViewCount() : 0) * 0.1;
        
        // 计算时间衰减
        long timeDiff = System.currentTimeMillis() - post.getCreateTime().getTime();
        double hours = timeDiff / (1000.0 * 60 * 60);
        double timeDecay = (hours / 24.0) * 10.0;
        
        // 最终分数
        double finalScore = score - timeDecay;
        return Math.max(finalScore, 0.0);
    }
    
    /**
     * 简化的转换方法（不包含用户状态）
     */
    private CommunityPostResponse convertToResponse(CommunityPost post) {
        CommunityPostResponse response = new CommunityPostResponse();
        response.setId(post.getId());
        response.setTitle(post.getTitle());
        response.setContent(post.getContent());
        response.setCategoryId(post.getCategoryId());
        response.setCategoryName(post.getCategoryName());
        response.setAuthorId(post.getAuthorId());
        response.setAuthorName(post.getAuthorName());
        response.setViewCount(post.getViewCount());
        response.setLikeCount(post.getLikeCount());
        response.setCommentCount(post.getCommentCount());
        response.setCollectCount(post.getCollectCount());
        response.setIsTop(post.getIsTop() != null && post.getIsTop() == 1);
        response.setCreateTime(post.getCreateTime());
        response.setIsLiked(false);
        response.setIsCollected(false);
        return response;
    }
}

