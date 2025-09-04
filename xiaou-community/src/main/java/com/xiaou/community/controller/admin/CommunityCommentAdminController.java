package com.xiaou.community.controller.admin;

import com.xiaou.common.annotation.Log;
import com.xiaou.common.annotation.RequireAdmin;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.core.domain.Result;
import com.xiaou.community.domain.CommunityComment;
import com.xiaou.community.dto.AdminCommentQueryRequest;
import com.xiaou.community.service.CommunityCommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 后台评论管理Controller
 * 
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/admin/community/comments")
@RequiredArgsConstructor
public class CommunityCommentAdminController {
    
    private final CommunityCommentService communityCommentService;
    
    /**
     * 评论列表（管理端）
     */
    @RequireAdmin
    @GetMapping
    public Result<PageResult<CommunityComment>> getCommentList(AdminCommentQueryRequest request) {
        PageResult<CommunityComment> result = communityCommentService.getAdminCommentList(request);
        return Result.success(result);
    }
    
    /**
     * 评论详情
     */
    @RequireAdmin
    @GetMapping("/{id}")
    public Result<CommunityComment> getComment(@PathVariable Long id) {
        CommunityComment comment = communityCommentService.getById(id);
        return Result.success(comment);
    }
    
    /**
     * 删除评论
     */
    @RequireAdmin
    @Log(module = "社区管理", type = Log.OperationType.DELETE, description = "删除评论")
    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        communityCommentService.deleteComment(id);
        return Result.success();
    }
} 