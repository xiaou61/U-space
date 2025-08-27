package com.xiaou.activity.controller;

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
 * 用户积分Controller
 */
@RestController
@RequestMapping("/user/points")
@Validated
public class UserPointsController {

    @Resource
    private UserPointsService userPointsService;

    @Resource
    private PointsRecordService pointsRecordService;

    /**
     * 查询当前用户积分余额
     */
    @GetMapping("/balance")
    public R<List<UserPointsResp>> getMyPointsBalance() {
        return userPointsService.getCurrentUserPointsBalance();
    }

    /**
     * 查询当前用户积分记录
     */
    @PostMapping("/records")
    public R<PageRespDto<PointsRecordResp>> getMyPointsRecords(@RequestBody @Validated PageReqDto req) {
        return pointsRecordService.pageCurrentUserPointsRecords(req);
    }
} 