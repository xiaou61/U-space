package com.xiaou.activity.controller;

import com.xiaou.activity.domain.entity.VoteActivity;
import com.xiaou.activity.domain.req.VoteActivityReq;
import com.xiaou.activity.domain.req.VoteOptionReq;
import com.xiaou.activity.domain.resp.VoteActivityResp;
import com.xiaou.activity.service.VoteActivityService;
import com.xiaou.activity.service.VoteOptionService;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/vote")
public class VoteAdminController {
    @Resource
    private VoteActivityService voteActivityService;
    @Resource
    private VoteOptionService voteOptionService;

    /**
     * 新增or修改活动
     *
     * @param req
     * @return
     */
    @PostMapping("/edit")
    public R<String> add(@RequestBody VoteActivityReq req) {
        return voteActivityService.add(req);
    }

    /**
     * 下架活动
     */
    @PostMapping("/offline")
    public R<String> offline(@RequestParam String id) {
        return voteActivityService.offline(id);
    }

    /**
     * 列出所有活动 分页 管理员用的，所以不用过滤其他东西
     */
    @PostMapping("/list")
    public R<PageRespDto<VoteActivityResp>> list(@RequestBody PageReqDto reqDto) {
        return voteActivityService.listPage(reqDto);
    }

    /**
     * 新增or修改活动选项
     */
    @PostMapping("/addOption")
    public R<String> addOption(@RequestParam String activityId, @RequestBody VoteOptionReq req) {
        return voteOptionService.addOption(activityId, req);
    }
    /**
     * 删除活动选项
     */
    @PostMapping("/deleteOption")
    public R<String> deleteOption(@RequestParam String optionId) {
        return voteOptionService.deleteOption(optionId);
    }
}
