package com.xiaou.community.controller.admin;

import com.xiaou.common.annotation.Log;
import com.xiaou.common.annotation.RequireAdmin;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.core.domain.Result;
import com.xiaou.community.domain.CommunityPost;
import com.xiaou.community.dto.AdminPostQueryRequest;
import com.xiaou.community.dto.PostTopRequest;
import com.xiaou.community.service.CommunityPostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台帖子管理Controller
 * 
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/admin/community/posts")
@RequiredArgsConstructor
public class CommunityPostAdminController {
    
    private final CommunityPostService communityPostService;
    
    /**
     * 帖子列表（管理端）
     */
    @RequireAdmin
    @PostMapping("/list")
    public Result<PageResult<CommunityPost>> getPostList(@RequestBody AdminPostQueryRequest request) {
        PageResult<CommunityPost> result = communityPostService.getAdminPostList(request);
        return Result.success(result);
    }
    
    /**
     * 帖子详情
     */
    @RequireAdmin
    @GetMapping("/{id}")
    public Result<CommunityPost> getPost(@PathVariable Long id) {
        CommunityPost post = communityPostService.getById(id);
        return Result.success(post);
    }
    
    /**
     * 置顶帖子
     */
    @RequireAdmin
    @Log(module = "社区管理", type = Log.OperationType.UPDATE, description = "置顶帖子")
    @PutMapping("/{id}/top")
    public Result<Void> topPost(@PathVariable Long id, @Validated @RequestBody PostTopRequest request) {
        communityPostService.topPost(id, request.getDuration());
        return Result.success();
    }
    
    /**
     * 取消置顶
     */
    @RequireAdmin
    @Log(module = "社区管理", type = Log.OperationType.UPDATE, description = "取消帖子置顶")
    @DeleteMapping("/{id}/top")
    public Result<Void> cancelTop(@PathVariable Long id) {
        communityPostService.cancelTop(id);
        return Result.success();
    }
    
    /**
     * 下架帖子
     */
    @RequireAdmin
    @Log(module = "社区管理", type = Log.OperationType.UPDATE, description = "下架帖子")
    @PutMapping("/{id}/disable")
    public Result<Void> disablePost(@PathVariable Long id) {
        communityPostService.disablePost(id);
        return Result.success();
    }
    
    /**
     * 删除帖子
     */
    @RequireAdmin
    @Log(module = "社区管理", type = Log.OperationType.DELETE, description = "删除帖子")
    @DeleteMapping("/{id}")
    public Result<Void> deletePost(@PathVariable Long id) {
        communityPostService.deletePost(id);
        return Result.success();
    }
} 