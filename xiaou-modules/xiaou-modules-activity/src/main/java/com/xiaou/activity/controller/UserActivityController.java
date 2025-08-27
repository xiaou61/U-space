package com.xiaou.activity.controller;

import com.xiaou.activity.domain.resp.ActivityResp;
import com.xiaou.activity.service.ActivityService;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;



/**
 * 用户活动控制器
 */
@RestController
@RequestMapping("/user/activity")
@Validated
public class UserActivityController {

    @Resource
    private ActivityService activityService;

    /**
     * 获取可参与的活动列表
     */
    @PostMapping("/available")
    public R<PageRespDto<ActivityResp>> getAvailableActivities(@RequestBody PageReqDto req) {
        return activityService.pageAvailableActivities(req);
    }

    /**
     * 获取活动详情
     */
    @GetMapping("/{id}")
    public R<ActivityResp> getActivity(@PathVariable Long id) {
        return activityService.getActivityById(id);
    }

    /**
     * 参与活动（抢夺）
     */
    @PostMapping("/{id}/participate")
    public R<Void> participateActivity(@PathVariable Long id) {
        return activityService.participateActivity(id);
    }

    /**
     * 取消参与活动
     */
    @DeleteMapping("/{id}/participate")
    public R<Void> cancelParticipation(@PathVariable Long id) {
        return activityService.cancelParticipation(id);
    }

    /**
     * 查询我参与的活动
     */
    @PostMapping("/my-activities")
    public R<PageRespDto<ActivityResp>> getMyActivities(@RequestBody PageReqDto req) {
        return activityService.getUserParticipatedActivities(req);
    }
} 