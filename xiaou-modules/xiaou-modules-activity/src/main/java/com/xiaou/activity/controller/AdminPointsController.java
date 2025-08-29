package com.xiaou.activity.controller;

import com.xiaou.activity.domain.req.PointsGrantReq;
import com.xiaou.activity.domain.resp.PointsRecordResp;
import com.xiaou.activity.domain.resp.UserPointsResp;
import com.xiaou.activity.service.PointsRecordService;
import com.xiaou.activity.service.UserPointsService;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理员积分Controller
 */
@RestController
@RequestMapping("/admin/points")
@Validated
public class AdminPointsController {

    @Resource
    private PointsRecordService pointsRecordService;

    @Resource
    private UserPointsService userPointsService;

    /**
     * 批量发放积分
     */
    @PostMapping("/grant")
    public R<Void> grantPointsManually(@RequestBody @Validated PointsGrantReq req) {
        return pointsRecordService.grantPointsManually(req);
    }

    /**
     * 撤销积分发放
     */
    @DeleteMapping("/revoke/{recordId}")
    public R<Void> revokePointsRecord(@PathVariable Long recordId) {
        return pointsRecordService.revokePointsRecord(recordId);
    }

    /**
     * 为活动参与者批量发放积分
     */
    @PostMapping("/activity/{activityId}/batch-grant")
    public R<Void> batchGrantPointsForActivity(@PathVariable Long activityId) {
        return pointsRecordService.batchGrantPointsForActivity(activityId);
    }

    /**
     * 分页查询积分记录
     */
    @PostMapping("/records")
    public R<PageRespDto<PointsRecordResp>> getPointsRecords(@RequestBody @Validated PageReqDto req) {
        return pointsRecordService.pagePointsRecords(req);
    }

    /**
     * 获取用户积分余额
     */
    @GetMapping("/balance/{userId}")
    public R<List<UserPointsResp>> getUserPointsBalance(@PathVariable String userId) {
        return userPointsService.getUserPointsBalance(userId);
    }
} 