package com.xiaou.auth.user.controller;

import cn.dev33.satoken.util.SaResult;
import com.alibaba.fastjson2.JSON;
import com.xiaou.auth.user.domain.req.StudentLoginReq;
import com.xiaou.auth.user.domain.req.StudentRegisterReq;
import com.xiaou.auth.user.domain.resp.StudentInfoResp;
import com.xiaou.auth.user.domain.resp.StudentLoginClassResp;
import com.xiaou.auth.user.service.StudentService;
import com.xiaou.common.domain.R;
import com.xiaou.log.annotation.Log;
import com.xiaou.log.enums.BusinessType;
import com.xiaou.log.enums.OperatorType;
import com.xiaou.ratelimiter.annotation.RateLimiter;
import com.xiaou.secure.annotation.SecureApi;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    @Log(title = "登录", businessType = BusinessType.LOGIN, operatorType = OperatorType.MOBILE)
    @PostMapping("/login")
    @SecureApi
    public R<SaResult> login(HttpServletRequest request) {
        String json = (String) request.getAttribute("secureData");
        StudentLoginReq req = JSON.parseObject(json, StudentLoginReq.class);
        return studentService.login(req);
    }
    /**
     * 学生登录不需要验证版本 测试用的
     */
    @PostMapping("/loginNoVerify")
    public R<SaResult> loginNoVerify(@RequestBody StudentLoginReq req) {
        return studentService.login(req);
    }

    /**
     * 查看本人信息
     *
     * @return
     */
    @GetMapping("/info")
    public R<StudentInfoResp> getInfo() {
        return studentService.getInfo();
    }

    /**
     * 上传头像
     */
    @Log(title = "上传头像", businessType = BusinessType.UPDATE, operatorType = OperatorType.MOBILE)
    @RateLimiter(key = "uploadAvatar", count = 5)
    @PostMapping("/uoloadavatar")
    public R<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        return studentService.uploadAvatar(file);
    }

    /**
     * 修改头像
     */
    @Log(title = "修改头像", businessType = BusinessType.UPDATE, operatorType = OperatorType.MOBILE)
    @PostMapping("/avatar")
    public R<String> updateAvatar(@RequestParam String avatar) {
        return studentService.updateAvatar(avatar);
    }

    /**
     * 修改密码
     */
    @Log(title = "修改密码", businessType = BusinessType.UPDATE, operatorType = OperatorType.MOBILE)
    @RateLimiter(key = "updatePassword", count = 5)
    @PostMapping("/password")
    public R<String> updatePassword(@RequestParam String oldPassword, @RequestParam String newPassword) {
        return studentService.updatePassword(oldPassword, newPassword);
    }

    /**
     * 查询所有班级
     */
    @PostMapping("/list")
    public R<List<StudentLoginClassResp>> list() {
        return studentService.listAll();
    }

    /**
     * 登出
     */
    @PostMapping("/logout")
    public R<String> logout() {
        return studentService.logout();
    }
    @Log(title = "测试接口123", businessType = BusinessType.UPDATE, operatorType = OperatorType.MOBILE)
    @GetMapping
    public R<String> test() {
        return R.ok("测试成功");
    }
}
