package com.xiaou.plan.controller.user;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.core.domain.Result;
import com.xiaou.common.satoken.StpUserUtil;
import com.xiaou.plan.domain.PlanCheckinRecord;
import com.xiaou.plan.dto.*;
import com.xiaou.plan.service.PlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端计划控制器
 * 
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/user/plan")
@RequiredArgsConstructor
public class UserPlanController {
    
    private final PlanService planService;
    
    /**
     * 创建计划
     */
    @PostMapping("/create")
    public Result<PlanResponse> createPlan(@RequestBody PlanCreateRequest request) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            PlanResponse response = planService.createPlan(userId, request);
            return Result.success("创建成功", response);
        } catch (Exception e) {
            log.error("创建计划失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新计划
     */
    @PutMapping("/update/{planId}")
    public Result<PlanResponse> updatePlan(@PathVariable Long planId, @RequestBody PlanCreateRequest request) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            PlanResponse response = planService.updatePlan(userId, planId, request);
            return Result.success("更新成功", response);
        } catch (Exception e) {
            log.error("更新计划失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除计划
     */
    @DeleteMapping("/{planId}")
    public Result<Boolean> deletePlan(@PathVariable Long planId) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            boolean success = planService.deletePlan(userId, planId);
            return success ? Result.success("删除成功", true) : Result.error("删除失败");
        } catch (Exception e) {
            log.error("删除计划失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取计划详情
     */
    @GetMapping("/{planId}")
    public Result<PlanResponse> getPlanDetail(@PathVariable Long planId) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            PlanResponse response = planService.getPlanDetail(userId, planId);
            return Result.success("获取成功", response);
        } catch (Exception e) {
            log.error("获取计划详情失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取计划列表
     */
    @PostMapping("/list")
    public Result<PageResult<PlanResponse>> getPlanList(@RequestBody PlanListRequest request) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            request.setUserId(userId);
            PageResult<PlanResponse> response = planService.getPlanList(request);
            return Result.success("获取成功", response);
        } catch (Exception e) {
            log.error("获取计划列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 暂停计划
     */
    @PutMapping("/{planId}/pause")
    public Result<Boolean> pausePlan(@PathVariable Long planId) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            boolean success = planService.pausePlan(userId, planId);
            return success ? Result.success("暂停成功", true) : Result.error("暂停失败");
        } catch (Exception e) {
            log.error("暂停计划失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 恢复计划
     */
    @PutMapping("/{planId}/resume")
    public Result<Boolean> resumePlan(@PathVariable Long planId) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            boolean success = planService.resumePlan(userId, planId);
            return success ? Result.success("恢复成功", true) : Result.error("恢复失败");
        } catch (Exception e) {
            log.error("恢复计划失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取今日任务列表
     */
    @GetMapping("/today-tasks")
    public Result<List<TodayTaskResponse>> getTodayTasks() {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            List<TodayTaskResponse> tasks = planService.getTodayTasks(userId);
            return Result.success("获取成功", tasks);
        } catch (Exception e) {
            log.error("获取今日任务失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 执行打卡
     */
    @PostMapping("/checkin")
    public Result<PlanCheckinResponse> checkin(@RequestBody PlanCheckinRequest request) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            PlanCheckinResponse response = planService.checkin(userId, request);
            return Result.success("打卡成功", response);
        } catch (Exception e) {
            log.error("打卡失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取计划打卡记录
     */
    @GetMapping("/{planId}/checkin/list")
    public Result<List<PlanCheckinRecord>> getCheckinRecords(@PathVariable Long planId) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            List<PlanCheckinRecord> records = planService.getCheckinRecords(userId, planId);
            return Result.success("获取成功", records);
        } catch (Exception e) {
            log.error("获取打卡记录失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取统计概览
     */
    @GetMapping("/stats/overview")
    public Result<PlanStatsResponse> getStatsOverview() {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            PlanStatsResponse response = planService.getStatsOverview(userId);
            return Result.success("获取成功", response);
        } catch (Exception e) {
            log.error("获取统计概览失败", e);
            return Result.error(e.getMessage());
        }
    }
}
