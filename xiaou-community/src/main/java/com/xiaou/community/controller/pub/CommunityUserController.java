package com.xiaou.community.controller.pub;

import com.xiaou.common.annotation.Log;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.core.domain.Result;
import com.xiaou.common.satoken.SaTokenUserUtil;
import com.xiaou.community.domain.CommunityPost;
import com.xiaou.community.domain.CommunityTag;
import com.xiaou.community.domain.CommunityUserStatus;
import com.xiaou.community.dto.*;
import com.xiaou.community.mapper.CommunityPostMapper;
import com.xiaou.community.mapper.CommunityPostTagMapper;
import com.xiaou.community.mapper.CommunityTagMapper;
import com.xiaou.community.service.CommunityCommentService;
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
    private final CommunityUserStatusService communityUserStatusService;
    private final CommunityPostMapper communityPostMapper;
    private final CommunityPostTagMapper communityPostTagMapper;
    private final CommunityTagMapper communityTagMapper;
    
    /**
     * 我的收藏列表
     */
    @Log(module = "社区", type = Log.OperationType.SELECT, description = "查询我的收藏列表")
    @PostMapping("/collections")
    public Result<PageResult<CommunityPostResponse>> getUserCollections(@RequestBody CommunityPostQueryRequest request) {
        PageResult<CommunityPostResponse> result = communityPostService.getUserCollections(request);
        return Result.success(result);
    }
    
    /**
     * 我的评论列表
     */
    @Log(module = "社区", type = Log.OperationType.SELECT, description = "查询我的评论列表")
    @PostMapping("/comments")
    public Result<PageResult<CommunityCommentResponse>> getUserComments(@RequestBody CommunityCommentQueryRequest request) {
        PageResult<CommunityCommentResponse> result = communityCommentService.getUserComments(request);
        return Result.success(result);
    }
    
    /**
     * 我的发帖列表
     */
    @Log(module = "社区", type = Log.OperationType.SELECT, description = "查询我的发帖列表")
    @PostMapping("/posts")
    public Result<PageResult<CommunityPostResponse>> getUserPosts(@RequestBody CommunityPostQueryRequest request) {
        PageResult<CommunityPostResponse> result = communityPostService.getUserPosts(request);
        return Result.success(result);
    }
}
 