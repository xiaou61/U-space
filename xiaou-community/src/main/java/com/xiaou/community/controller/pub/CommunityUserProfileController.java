package com.xiaou.community.controller.pub;

import cn.hutool.core.bean.BeanUtil;
import com.xiaou.common.annotation.Log;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.core.domain.Result;
import com.xiaou.common.exception.BusinessException;
import com.xiaou.common.satoken.SaTokenUserUtil;
import com.xiaou.community.domain.CommunityPost;
import com.xiaou.community.domain.CommunityTag;
import com.xiaou.community.domain.CommunityUserStatus;
import com.xiaou.community.dto.CommunityPostQueryRequest;
import com.xiaou.community.dto.CommunityPostResponse;
import com.xiaou.community.dto.CommunityUserProfileResponse;
import com.xiaou.community.mapper.CommunityPostMapper;
import com.xiaou.community.mapper.CommunityPostTagMapper;
import com.xiaou.community.mapper.CommunityTagMapper;
import com.xiaou.community.service.CommunityPostService;
import com.xiaou.community.service.CommunityUserStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 前台用户主页控制器
 * 
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/community/users")
@RequiredArgsConstructor
public class CommunityUserProfileController {
    
    private final CommunityUserStatusService communityUserStatusService;
    private final CommunityPostService communityPostService;
    private final CommunityPostMapper communityPostMapper;
    private final CommunityPostTagMapper communityPostTagMapper;
    private final CommunityTagMapper communityTagMapper;
    
    /**
     * 获取用户主页信息
     */
    @Log(module = "社区", type = Log.OperationType.SELECT, description = "查看用户主页")
    @GetMapping("/{userId}/profile")
    public Result<CommunityUserProfileResponse> getUserProfile(@PathVariable Long userId) {
        // 获取用户状态信息
        CommunityUserStatus userStatus = communityUserStatusService.getUserStatusByUserId(userId);
        if (userStatus == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 构建响应
        CommunityUserProfileResponse response = new CommunityUserProfileResponse();
        response.setUserId(userId);
        response.setUserName(userStatus.getUserName());
        
        // 获取用户头像和简介（从用户服务获取）
        try {
            String avatar = SaTokenUserUtil.getUserAvatarById(userId, "");
            String bio = SaTokenUserUtil.getUserBioById(userId, "这个人很懒，什么都没写");
            response.setAvatar(avatar);
            response.setBio(bio);
        } catch (Exception e) {
            log.warn("获取用户详细信息失败，使用默认值", e);
            response.setAvatar("");
            response.setBio("这个人很懒，什么都没写");
        }
        
        // 统计数据
        CommunityUserProfileResponse.UserStats stats = new CommunityUserProfileResponse.UserStats();
        stats.setPostCount(userStatus.getPostCount());
        stats.setLikeCount(userStatus.getLikeCount());
        stats.setCommentCount(userStatus.getCommentCount());
        stats.setCollectCount(userStatus.getCollectCount());
        response.setStats(stats);
        
        // 查询用户活跃标签（最多5个）
        List<Long> activeTagIds = communityPostTagMapper.selectUserActiveTagIds(userId, 5);
        if (activeTagIds != null && !activeTagIds.isEmpty()) {
            // 统计每个标签的使用次数
            Map<Long, Integer> tagCountMap = new HashMap<>();
            for (Long tagId : activeTagIds) {
                tagCountMap.put(tagId, tagCountMap.getOrDefault(tagId, 0) + 1);
            }
            
            List<CommunityUserProfileResponse.TagWithCount> activeTags = new ArrayList<>();
            for (Map.Entry<Long, Integer> entry : tagCountMap.entrySet()) {
                CommunityTag tag = communityTagMapper.selectById(entry.getKey());
                if (tag != null) {
                    CommunityUserProfileResponse.TagWithCount tagWithCount = 
                        new CommunityUserProfileResponse.TagWithCount();
                    tagWithCount.setId(tag.getId());
                    tagWithCount.setName(tag.getName());
                    tagWithCount.setColor(tag.getColor());
                    tagWithCount.setCount(entry.getValue());
                    activeTags.add(tagWithCount);
                }
            }
            response.setActiveTags(activeTags);
        }
        
        // 查询最近3篇帖子
        List<CommunityPost> recentPosts = communityPostMapper.selectRecentPostsByUserId(userId, 3);
        if (recentPosts != null && !recentPosts.isEmpty()) {
            List<CommunityPostResponse> recentPostResponses = recentPosts.stream()
                .map(post -> {
                    CommunityPostResponse postResponse = BeanUtil.copyProperties(post, CommunityPostResponse.class);
                    postResponse.setIsTop(post.getIsTop() != null && post.getIsTop() == 1);
                    return postResponse;
                })
                .collect(Collectors.toList());
            response.setRecentPosts(recentPostResponses);
        }
        
        return Result.success(response);
    }
    
    /**
     * 获取指定用户的帖子列表
     */
    @Log(module = "社区", type = Log.OperationType.SELECT, description = "查询用户帖子列表")
    @PostMapping("/{userId}/posts")
    public Result<PageResult<CommunityPostResponse>> getUserPostList(
            @PathVariable Long userId,
            @RequestBody CommunityPostQueryRequest request) {
        // 直接查询该用户的帖子（不需要登录）
        PageResult<CommunityPost> pageResult = com.xiaou.common.utils.PageHelper.doPage(
            request.getPageNum(), 
            request.getPageSize(), 
            () -> {
                List<CommunityPost> posts = communityPostMapper.selectUserPostList(userId, request);
                return posts;
            }
        );
        
        // 转换为响应DTO
        List<CommunityPostResponse> responseList = pageResult.getRecords().stream()
            .map(post -> {
                CommunityPostResponse response = BeanUtil.copyProperties(post, CommunityPostResponse.class);
                response.setIsTop(post.getIsTop() != null && post.getIsTop() == 1);
                return response;
            })
            .collect(Collectors.toList());
        
        PageResult<CommunityPostResponse> result = PageResult.of(
            pageResult.getPageNum(),
            pageResult.getPageSize(),
            pageResult.getTotal(),
            responseList
        );
        
        return Result.success(result);
    }
}

