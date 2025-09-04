package com.xiaou.community.controller.pub;

import com.xiaou.common.annotation.Log;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.core.domain.Result;
import com.xiaou.community.dto.CommunityPostQueryRequest;
import com.xiaou.community.dto.CommunityCommentQueryRequest;
import com.xiaou.community.dto.CommunityPostResponse;
import com.xiaou.community.dto.CommunityCommentResponse;
import com.xiaou.community.service.CommunityPostService;
import com.xiaou.community.service.CommunityCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 前台用户个人中心控制器
 * 
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/community/user")
@RequiredArgsConstructor
public class CommunityUserController {
    
    private final CommunityPostService communityPostService;
    private final CommunityCommentService communityCommentService;
    
    /**
     * 我的收藏列表
     */
    @Log(module = "社区", type = Log.OperationType.SELECT, description = "查询我的收藏列表")
    @GetMapping("/collections")
    public Result<PageResult<CommunityPostResponse>> getUserCollections(CommunityPostQueryRequest request) {
        PageResult<CommunityPostResponse> result = communityPostService.getUserCollections(request);
        return Result.success(result);
    }
    
    /**
     * 我的评论列表
     */
    @Log(module = "社区", type = Log.OperationType.SELECT, description = "查询我的评论列表")
    @GetMapping("/comments")
    public Result<PageResult<CommunityCommentResponse>> getUserComments(CommunityCommentQueryRequest request) {
        PageResult<CommunityCommentResponse> result = communityCommentService.getUserComments(request);
        return Result.success(result);
    }
    
    /**
     * 我的发帖列表
     */
    @Log(module = "社区", type = Log.OperationType.SELECT, description = "查询我的发帖列表")
    @GetMapping("/posts")
    public Result<PageResult<CommunityPostResponse>> getUserPosts(CommunityPostQueryRequest request) {
        PageResult<CommunityPostResponse> result = communityPostService.getUserPosts(request);
        return Result.success(result);
    }
} 