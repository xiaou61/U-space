package com.xiaou.study.group.student.controller;

import com.xiaou.common.domain.R;
import com.xiaou.study.group.teacher.domain.entity.SigninRecord;
import com.xiaou.study.group.teacher.domain.req.SigninRecordReq;
import com.xiaou.study.group.teacher.domain.resp.SigninResp;
import com.xiaou.study.group.teacher.service.SigninRecordService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/student/signin")
@Validated
public class SigninController {
    @Resource
    private SigninRecordService signinRecordService;
    /**
     * 签到
     */
    @PostMapping("/add")
    public R<SigninRecord> add(@RequestBody SigninRecordReq req) {
        return signinRecordService.add(req);
    }
    /**
     * 查看需要签到的列表
     */
    @PostMapping("/list")
    public R<List<SigninResp>> list(@RequestParam String groupId) {
        return signinRecordService.listMy(groupId);
    }
}
