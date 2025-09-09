package com.xiaou.moment.controller.user;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.core.domain.Result;
import com.xiaou.moment.dto.*;
import com.xiaou.moment.service.MomentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端动态Controller
 */
@RestController
@RequestMapping("/user/moments")
@RequiredArgsConstructor
public class UserMomentController {
    
    private final MomentService momentService;
    
    /**
     * 发布动态
     */
    @PostMapping("/publish")
    public Result<Long> publishMoment(@Valid @RequestBody MomentPublishRequest request) {
        Long momentId = momentService.publishMoment(request);
        return Result.success(momentId);
    }
    
    /**
     * 获取动态列表
     */
    @PostMapping("/list")
    public Result<PageResult<MomentListResponse>> getMomentList(@RequestBody UserMomentListRequest request) {
        PageResult<MomentListResponse> response = momentService.getMomentList(request);
        return Result.success(response);
    }
    
    /**
     * 删除动态
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteMoment(@PathVariable Long id) {
        momentService.deleteMoment(id);
        return Result.success();
    }
    
    /**
     * 点赞/取消点赞
     */
    @PostMapping("/{momentId}/like")
    public Result<Boolean> toggleLike(@PathVariable Long momentId) {
        Boolean isLiked = momentService.toggleLike(momentId);
        return Result.success(isLiked);
    }
    
    /**
     * 发布评论
     */
    @PostMapping("/comment")
    public Result<Long> publishComment(@Valid @RequestBody CommentPublishRequest request) {
        Long commentId = momentService.publishComment(request);
        return Result.success(commentId);
    }
    
    /**
     * 删除评论
     */
    @DeleteMapping("/comments/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        momentService.deleteComment(id);
        return Result.success();
    }
    
    /**
     * 获取动态的完整评论列表
     */
    @PostMapping("/comments")
    public Result<PageResult<CommentResponse>> getMomentComments(@RequestBody UserMomentCommentsRequest request) {
        PageResult<CommentResponse> response = momentService.getMomentComments(request);
        return Result.success(response);
    }
} 