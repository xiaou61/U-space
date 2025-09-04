package com.xiaou.community.controller.admin;

import com.xiaou.common.annotation.Log;
import com.xiaou.common.annotation.RequireAdmin;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.core.domain.Result;
import com.xiaou.community.domain.CommunityComment;
import com.xiaou.community.domain.CommunityPost;
import com.xiaou.community.domain.CommunityUserStatus;
import com.xiaou.community.dto.AdminUserQueryRequest;
import com.xiaou.community.dto.UserBanRequest;
import com.xiaou.community.service.CommunityCommentService;
import com.xiaou.community.service.CommunityPostService;
import com.xiaou.community.service.CommunityUserStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台用户管理Controller
 * 
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/admin/community/users")
@RequiredArgsConstructor
public class CommunityUserAdminController {
    
    private final CommunityUserStatusService communityUserStatusService;
    private final CommunityPostService communityPostService;
    private final CommunityCommentService communityCommentService;
    
    /**
     * 用户社区状态列表
     */
    @RequireAdmin
    @GetMapping
    public Result<PageResult<CommunityUserStatus>> getUserList(AdminUserQueryRequest request) {
        PageResult<CommunityUserStatus> result = communityUserStatusService.getAdminUserList(request);
        return Result.success(result);
    }
    
    /**
     * 用户社区状态详情
     */
    @RequireAdmin
    @GetMapping("/{userId}")
    public Result<CommunityUserStatus> getUserStatus(@PathVariable Long userId) {
        CommunityUserStatus userStatus = communityUserStatusService.getByUserId(userId);
        return Result.success(userStatus);
    }
    
    /**
     * 封禁用户
     */
    @RequireAdmin
    @Log(module = "社区管理", type = Log.OperationType.UPDATE, description = "封禁用户")
    @PutMapping("/{userId}/ban")
    public Result<Void> banUser(@PathVariable Long userId, @Validated @RequestBody UserBanRequest request) {
        communityUserStatusService.banUser(userId, request.getReason(), request.getDuration());
        return Result.success();
    }
    
    /**
     * 解封用户
     */
    @RequireAdmin
    @Log(module = "社区管理", type = Log.OperationType.UPDATE, description = "解封用户")
    @DeleteMapping("/{userId}/ban")
    public Result<Void> unbanUser(@PathVariable Long userId) {
        communityUserStatusService.unbanUser(userId);
        return Result.success();
    }
    
    /**
     * 用户发帖记录
     */
    @RequireAdmin
    @GetMapping("/{userId}/posts")
    public Result<List<CommunityPost>> getUserPosts(@PathVariable Long userId) {
        List<CommunityPost> posts = communityPostService.getPostsByUserId(userId);
        return Result.success(posts);
    }
    
    /**
     * 用户评论记录
     */
    @RequireAdmin
    @GetMapping("/{userId}/comments")
    public Result<List<CommunityComment>> getUserComments(@PathVariable Long userId) {
        List<CommunityComment> comments = communityCommentService.getCommentsByUserId(userId);
        return Result.success(comments);
    }
} 