package com.xiaou.study.group.student.controller;

import com.xiaou.common.domain.R;
import com.xiaou.study.group.teacher.domain.entity.SigninRecord;
import com.xiaou.study.group.teacher.domain.req.SigninRecordReq;
import com.xiaou.study.group.teacher.serivce.SigninRecordService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
