package com.xiaou.activity.controller;

import com.xiaou.activity.domain.req.ActivityCreateReq;
import com.xiaou.activity.domain.req.ActivityUpdateReq;
import com.xiaou.activity.domain.resp.ActivityResp;
import com.xiaou.activity.domain.resp.ActivityParticipantResp;
import com.xiaou.activity.schedule.ActivityPointsScheduleTask;
import com.xiaou.activity.service.ActivityService;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;



/**
 * 管理员活动控制器
 */
@RestController
@RequestMapping("/admin/activity")
@Validated
public class AdminActivityController {

    @Resource
    private ActivityService activityService;

    @Autowired
    private ActivityPointsScheduleTask activityPointsScheduleTask;

    /**
     * 创建活动
     */
    @PostMapping
    public R<Long> createActivity(@RequestBody @Validated ActivityCreateReq req) {
        return activityService.createActivity(req);
    }

    /**
     * 更新活动
     */
    @PutMapping("/{id}")
    public R<Void> updateActivity(@PathVariable Long id, @RequestBody @Validated ActivityUpdateReq req) {
        req.setId(id);
        return activityService.updateActivity(req);
    }

    /**
     * 删除活动
     */
    @DeleteMapping("/{id}")
    public R<Void> deleteActivity(@PathVariable Long id) {
        return activityService.deleteActivity(id);
    }

    /**
     * 获取活动详情
     */
    @GetMapping("/{id}")
    public R<ActivityResp> getActivity(@PathVariable Long id) {
        return activityService.getActivityById(id);
    }

    /**
     * 分页查询活动列表
     */
    @PostMapping("/page")
    public R<PageRespDto<ActivityResp>> pageActivities(@RequestBody PageReqDto req) {
        return activityService.pageActivitiesAdmin(req);
    }

    /**
     * 启用活动（重新计算状态）
     */
    @PutMapping("/{id}/enable")
    public R<Void> enableActivity(@PathVariable Long id) {
        return activityService.publishActivity(id);
    }

    /**
     * 禁用活动（暂停活动）
     */
    @PutMapping("/{id}/disable") 
    public R<Void> disableActivity(@PathVariable Long id) {
        return activityService.disableActivity(id);
    }

    /**
     * 取消活动（永久取消）
     */
    @PutMapping("/{id}/cancel")
    public R<Void> cancelActivity(@PathVariable Long id) {
        return activityService.cancelActivity(id);
    }

    /**
     * 获取活动参与者列表
     */
    @PostMapping("/{id}/participants")
    public R<PageRespDto<ActivityParticipantResp>> getActivityParticipants(
            @PathVariable Long id,
            @RequestBody PageReqDto req) {
        return activityService.getActivityParticipants(id, req);
    }

    /**
     * 活动结束后自动发放积分
     */
    @PostMapping("/{id}/auto-grant")
    public R<Void> autoGrantPoints(@PathVariable Long id) {
        return activityService.autoGrantPointsAfterActivity(id);
    }

    /**
     * 批量处理已结束活动的积分发放（手动触发定时任务）
     */
    @PostMapping("/batch-grant-points")
    public R<String> batchGrantPoints() {
        try {
            activityPointsScheduleTask.manualTriggerPointsGrant();
            return R.ok("批量积分发放任务已触发，请查看系统日志了解详细情况");
        } catch (Exception e) {
            return R.fail("批量积分发放任务触发失败：" + e.getMessage());
        }
    }

    /**
     * 批量更新活动状态（手动触发定时任务）
     */
    @PostMapping("/batch-update-status")
    public R<String> batchUpdateStatus() {
        try {
            activityPointsScheduleTask.manualTriggerStatusUpdate();
            return R.ok("批量状态更新任务已触发，请查看系统日志了解详细情况");
        } catch (Exception e) {
            return R.fail("批量状态更新任务触发失败：" + e.getMessage());
        }
    }
} 