package com.xiaou.community.controller.pub;

import com.xiaou.common.annotation.Log;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.core.domain.Result;
import com.xiaou.community.dto.CommunityCommentQueryRequest;
import com.xiaou.community.dto.CommunityCommentCreateRequest;
import com.xiaou.community.dto.CommunityCommentReplyRequest;
import com.xiaou.community.dto.CommunityCommentResponse;
import com.xiaou.community.service.CommunityCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 前台评论控制器
 * 
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/community")
@RequiredArgsConstructor
public class CommunityCommentController {
    
    private final CommunityCommentService communityCommentService;
    
    /**
     * 获取帖子的评论列表
     */
    @Log(module = "社区", type = Log.OperationType.SELECT, description = "查询帖子评论列表")
    @PostMapping("/posts/{postId}/comments")
    public Result<PageResult<CommunityCommentResponse>> getPostComments(
            @PathVariable Long postId,
            @RequestBody CommunityCommentQueryRequest request) {
        PageResult<CommunityCommentResponse> result = communityCommentService.getPostComments(postId, request);
        return Result.success(result);
    }
    
    /**
     * 发表评论
     */
    @Log(module = "社区", type = Log.OperationType.INSERT, description = "发表评论")
    @PostMapping("/posts/{postId}/comments/create")
    public Result<Void> createComment(
            @PathVariable Long postId,
            @Validated @RequestBody CommunityCommentCreateRequest request) {
        communityCommentService.createComment(postId, request);
        return Result.success();
    }
    
    /**
     * 点赞评论
     */
    @Log(module = "社区", type = Log.OperationType.UPDATE, description = "点赞评论")
    @PostMapping("/comments/{id}/like")
    public Result<Void> likeComment(@PathVariable Long id) {
        communityCommentService.likeComment(id);
        return Result.success();
    }
    
    /**
     * 取消点赞评论
     */
    @Log(module = "社区", type = Log.OperationType.UPDATE, description = "取消点赞评论")
    @DeleteMapping("/comments/{id}/like")
    public Result<Void> unlikeComment(@PathVariable Long id) {
        communityCommentService.unlikeComment(id);
        return Result.success();
    }
    
    /**
     * 回复评论
     */
    @Log(module = "社区", type = Log.OperationType.INSERT, description = "回复评论")
    @PostMapping("/comments/{id}/reply")
    public Result<Void> replyComment(
            @PathVariable Long id,
            @Validated @RequestBody CommunityCommentReplyRequest request) {
        communityCommentService.replyComment(id, request);
        return Result.success();
    }
    
    /**
     * 获取评论的回复列表
     */
    @Log(module = "社区", type = Log.OperationType.SELECT, description = "查询评论回复列表")
    @PostMapping("/comments/{id}/replies")
    public Result<PageResult<CommunityCommentResponse>> getCommentReplies(
            @PathVariable Long id,
            @RequestBody CommunityCommentQueryRequest request) {
        PageResult<CommunityCommentResponse> result = communityCommentService.getCommentReplies(id, request);
        return Result.success(result);
    }
} 