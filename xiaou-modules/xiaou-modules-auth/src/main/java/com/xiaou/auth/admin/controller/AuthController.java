package com.xiaou.auth.admin.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.xiaou.auth.admin.domain.entity.AdminUser;
import com.xiaou.auth.admin.domain.req.AdminUserReq;
import com.xiaou.auth.admin.service.AdminUserService;
import com.xiaou.common.domain.R;
import com.xiaou.satoken.utils.LoginHelper;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/auth")
@Validated
public class AuthController {
    @Resource
    private AdminUserService baseService;
    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public R<SaResult> login(@RequestBody AdminUserReq req) {
        return baseService.login(req);
    }
    /**
     * 获得当前管理员信息
     */
    @GetMapping("/info")
    public R<AdminUser> getInfo(){
        Long userId = LoginHelper.getCurrentAppUserId();
        return R.ok(baseService.getById(userId));
    }
    /**
     * 管理员注销
     */
    @GetMapping("/logout")
    public R<String> logout(){
        StpUtil.logout(LoginHelper.getCurrentAppUserId());
        return R.ok("注销成功");
    }
}
