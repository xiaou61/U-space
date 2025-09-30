package com.xiaou.points.controller.admin;

import com.xiaou.common.annotation.RequireAdmin;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.core.domain.Result;
import com.xiaou.common.satoken.StpAdminUtil;
import com.xiaou.points.dto.*;
import com.xiaou.points.service.PointsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端积分控制器
 * 
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/admin/points")
@RequiredArgsConstructor
public class AdminPointsController {
    
    private final PointsService pointsService;
    
    /**
     * 管理员发放积分
     */
    @RequireAdmin(message = "需要管理员权限才能发放积分")
    @PostMapping("/grant")
    public Result<AdminGrantPointsResponse> grantPoints(@Valid @RequestBody AdminGrantPointsRequest request) {
        try {
            Long adminId = StpAdminUtil.getLoginIdAsLong();
            
            AdminGrantPointsResponse response = pointsService.grantPoints(request, adminId);
            
            log.info("管理员{}为用户{}发放{}积分成功，原因：{}", 
                    adminId, request.getUserId(), request.getPoints(), request.getReason());
            
            return Result.success("积分发放成功", response);
            
        } catch (Exception e) {
            log.error("管理员发放积分失败，请求：{}", request, e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取所有用户积分明细列表
     */
    @RequireAdmin(message = "需要管理员权限才能查看所有用户积分明细")
    @PostMapping("/detail-list")
    public Result<PageResult<PointsDetailResponse>> getAllPointsDetailList(@RequestBody PointsDetailQueryRequest request) {
        try {
            PageResult<PointsDetailResponse> response = pointsService.getAllPointsDetailList(request);
            return Result.success("获取成功", response);
            
        } catch (Exception e) {
            log.error("获取所有积分明细失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取用户积分列表（用于积分排行和管理）
     */
    @RequireAdmin(message = "需要管理员权限才能查看用户积分列表")
    @PostMapping("/user-list")
    public Result<PageResult<UserPointsRankingResponse>> getUserPointsList(@RequestBody UserPointsListRequest request) {
        try {
            PageResult<UserPointsRankingResponse> response = pointsService.getUserPointsList(request);
            return Result.success("获取成功", response);
            
        } catch (Exception e) {
            log.error("获取用户积分列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取积分统计数据
     */
    @RequireAdmin(message = "需要管理员权限才能查看积分统计")
    @GetMapping("/statistics")
    public Result<AdminPointsStatisticsResponse> getPointsStatistics() {
        try {
            AdminPointsStatisticsResponse response = pointsService.getAdminStatistics();
            return Result.success("获取成功", response);
            
        } catch (Exception e) {
            log.error("获取积分统计数据失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 批量发放积分
     */
    @RequireAdmin(message = "需要管理员权限才能批量发放积分")
    @PostMapping("/batch-grant")
    public Result<BatchGrantPointsResponse> batchGrantPoints(@Valid @RequestBody BatchGrantPointsRequest request) {
        try {
            UserContextUtil.UserInfo currentUser = userContextUtil.getCurrentUser();
            BatchGrantPointsResponse response = pointsService.batchGrantPoints(request, currentUser.getId());
            
            log.info("管理员{}批量发放积分完成，成功{}人，失败{}人，总计发放{}积分", 
                    currentUser.getUsername(), response.getSuccessCount(), 
                    response.getFailCount(), response.getTotalPointsGranted());
            
            return Result.success("批量发放完成", response);
            
        } catch (Exception e) {
            log.error("批量发放积分失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 根据用户ID获取用户积分信息
     */
    @RequireAdmin(message = "需要管理员权限才能查看用户积分信息")
    @GetMapping("/user-info/{userId}")
    public Result<UserPointsInfoResponse> getUserPointsInfo(@PathVariable Long userId) {
        try {
            UserPointsInfoResponse response = pointsService.getUserPointsInfo(userId);
            return Result.success("获取成功", response);
            
        } catch (Exception e) {
            log.error("获取用户积分信息失败，userId: {}", userId, e);
            return Result.error(e.getMessage());
        }
    }
}
