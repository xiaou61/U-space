package com.xiaou.moment.controller.admin;

import com.xiaou.common.annotation.RequireAdmin;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.core.domain.Result;
import com.xiaou.moment.dto.*;
import com.xiaou.moment.service.MomentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端动态Controller
 */
@RestController
@RequestMapping("/admin/moments")
@RequiredArgsConstructor
public class AdminMomentController {
    
    private final MomentService momentService;
    
    /**
     * 获取动态列表（管理端）
     */
    @PostMapping("/list")
    @RequireAdmin
    public Result<PageResult<AdminMomentListResponse>> getMomentList(@RequestBody AdminMomentListRequest request) {
        PageResult<AdminMomentListResponse> response = momentService.getAdminMomentList(request);
        return Result.success(response);
    }
    
    /**
     * 批量删除动态
     */
    @PostMapping("/batch-delete")
    @RequireAdmin
    public Result<Void> batchDeleteMoments(@RequestBody List<Long> ids) {
        momentService.batchDeleteMoments(ids);
        return Result.success();
    }
    
    /**
     * 获取评论列表（管理端）
     */
    @PostMapping("/comments/list")
    @RequireAdmin
    public Result<PageResult<AdminCommentListResponse>> getCommentList(@RequestBody AdminCommentListRequest request) {
        PageResult<AdminCommentListResponse> response = momentService.getAdminCommentList(request);
        return Result.success(response);
    }
    
    /**
     * 删除评论（管理端）
     */
    @DeleteMapping("/comments/{id}")
    @RequireAdmin
    public Result<Void> deleteComment(@PathVariable Long id) {
        momentService.adminDeleteComment(id);
        return Result.success();
    }
    
    /**
     * 获取统计数据
     */
    @PostMapping("/statistics")
    @RequireAdmin
    public Result<MomentStatisticsResponse> getStatistics(@RequestBody StatisticsRequest request) {
        MomentStatisticsResponse response = momentService.getStatistics(request);
        return Result.success(response);
    }
} 