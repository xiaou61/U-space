package com.xiaou.auth.user.controller;

import cn.dev33.satoken.util.SaResult;
import com.xiaou.auth.user.domain.req.StudentLoginReq;
import com.xiaou.auth.user.domain.req.StudentRegisterReq;
import com.xiaou.auth.user.domain.resp.StudentInfoResp;
import com.xiaou.auth.user.domain.resp.StudentLoginClassResp;
import com.xiaou.auth.user.service.StudentService;
import com.xiaou.common.domain.R;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/student/auth")
@Validated
public class StudentAuthController {
    @Resource
    private StudentService studentService;

    /**
     * 学生注册
     */
    @PostMapping("/register")
    public R<String> register(@RequestBody StudentRegisterReq req) {
        return studentService.register(req);
    }

    /**
     * 学生登录
     */
    @PostMapping("/login")
    public R<SaResult> login(@RequestBody StudentLoginReq req) {
        return studentService.login(req);
    }
    @GetMapping("/info")
    public R<StudentInfoResp> getInfo() {
        return studentService.getInfo();
    }

    /**
     * 查询所有班级
     */
    @PostMapping("/list")
    public R<List<StudentLoginClassResp>> list() {
        return studentService.listAll();
    }
}
