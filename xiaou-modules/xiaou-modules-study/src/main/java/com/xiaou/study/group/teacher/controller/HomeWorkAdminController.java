package com.xiaou.study.group.teacher.controller;

import com.xiaou.common.domain.R;
import com.xiaou.study.group.teacher.domain.req.HomeworkApproveReq;
import com.xiaou.study.group.teacher.domain.req.HomeworkReq;
import com.xiaou.study.group.teacher.domain.resp.HomeworkResp;
import com.xiaou.study.group.teacher.domain.resp.HomeworkSubmissionResp;
import com.xiaou.study.group.teacher.service.HomeworkService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teacher/homework")
@Validated
public class HomeWorkAdminController {
    @Resource
    private HomeworkService homeworkService;
    /**
     * 创建一个作业
     */
    @PostMapping("/add")
    public R<String> add(@RequestBody HomeworkReq req) {
        return homeworkService.add(req);
    }

    /**
     * 查看群组作业列表
     */
    @PostMapping("/list")
    public R<List<HomeworkResp>> list(@RequestParam String groupId) {
        return homeworkService.listAll(groupId);
    }
    /**
     * 查询某个作业详细
     */
    @PostMapping("/homework-detail")
    public R<HomeworkResp> detailHome(@RequestParam String id) {
        return homeworkService.detailHome(id);
    }

    /**
     * 查看详细作业id的提交的详细情况
     * @id homework_id
     */
    @PostMapping("/detail")
    public R<List<HomeworkSubmissionResp>> detail(@RequestParam String id) {
        return homeworkService.detail(id);
    }
    /**
     * 查看某个作业提交情况
     */
    @PostMapping("/homework")
    public R<HomeworkSubmissionResp> homework(@RequestParam String id) {
        return homeworkService.homework(id);
    }
    /**
     * 审批某个作业
     */
    @PostMapping("/approve")
    public R<String> approve(@RequestParam String id,@RequestBody HomeworkApproveReq req) {
        return homeworkService.approve(id,req);
    }
}
