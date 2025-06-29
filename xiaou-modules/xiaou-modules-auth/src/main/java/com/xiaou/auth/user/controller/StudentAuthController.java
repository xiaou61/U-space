package com.xiaou.auth.user.controller;

import com.xiaou.auth.user.domain.req.StudentRegisterReq;
import com.xiaou.auth.user.service.StudentService;
import com.xiaou.common.domain.R;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/student/auth")
@Validated
public class StudentAuthController {
    @Resource
    private StudentService studentService;

    /**
     * 学生注册
     */
    public R<String> register(@RequestBody StudentRegisterReq req) {
        return studentService.register(req);
    }
}
