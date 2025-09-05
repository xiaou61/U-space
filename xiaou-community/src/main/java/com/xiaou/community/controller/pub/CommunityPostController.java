package com.xiaou.community.controller.pub;

import com.xiaou.common.annotation.Log;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.core.domain.Result;
import com.xiaou.community.dto.CommunityPostQueryRequest;
import com.xiaou.community.dto.CommunityPostCreateRequest;
import com.xiaou.community.dto.CommunityPostResponse;
import com.xiaou.community.service.CommunityPostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 前台帖子控制器
 * 
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/community/posts")
@RequiredArgsConstructor
public class CommunityPostController {
    
    private final CommunityPostService communityPostService;
    
    /**
     * 帖子列表查询
     */
    @Log(module = "社区", type = Log.OperationType.SELECT, description = "查询帖子列表")
    @PostMapping("/list")
    public Result<PageResult<CommunityPostResponse>> getPostList(@RequestBody CommunityPostQueryRequest request) {
        log.info("Controller收到帖子列表查询请求: {}", request);
        PageResult<CommunityPostResponse> result = communityPostService.getPostList(request);
        log.info("Controller返回结果，total: {}", result.getTotal());
        return Result.success(result);
    }
    
    /**
     * 帖子详情
     */
    @Log(module = "社区", type = Log.OperationType.SELECT, description = "查询帖子详情")
    @GetMapping("/{id}")
    public Result<CommunityPostResponse> getPostDetail(@PathVariable Long id) {
        CommunityPostResponse result = communityPostService.getPostDetail(id);
        return Result.success(result);
    }
    
    /**
     * 创建帖子
     */
    @Log(module = "社区", type = Log.OperationType.INSERT, description = "创建帖子")
    @PostMapping
    public Result<Void> createPost(@Validated @RequestBody CommunityPostCreateRequest request) {
        communityPostService.createPost(request);
        return Result.success();
    }
    
    /**
     * 点赞帖子
     */
    @Log(module = "社区", type = Log.OperationType.UPDATE, description = "点赞帖子")
    @PostMapping("/{id}/like")
    public Result<Void> likePost(@PathVariable Long id) {
        communityPostService.likePost(id);
        return Result.success();
    }
    
    /**
     * 取消点赞帖子
     */
    @Log(module = "社区", type = Log.OperationType.UPDATE, description = "取消点赞帖子")
    @DeleteMapping("/{id}/like")
    public Result<Void> unlikePost(@PathVariable Long id) {
        communityPostService.unlikePost(id);
        return Result.success();
    }
    
    /**
     * 收藏帖子
     */
    @Log(module = "社区", type = Log.OperationType.UPDATE, description = "收藏帖子")
    @PostMapping("/{id}/collect")
    public Result<Void> collectPost(@PathVariable Long id) {
        communityPostService.collectPost(id);
        return Result.success();
    }
    
    /**
     * 取消收藏帖子
     */
    @Log(module = "社区", type = Log.OperationType.UPDATE, description = "取消收藏帖子")
    @DeleteMapping("/{id}/collect")
    public Result<Void> uncollectPost(@PathVariable Long id) {
        communityPostService.uncollectPost(id);
        return Result.success();
    }
} 