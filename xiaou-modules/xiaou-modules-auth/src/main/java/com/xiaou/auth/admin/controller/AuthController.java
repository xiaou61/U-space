package com.xiaou.auth.admin.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.xiaou.auth.admin.domain.entity.AdminUser;
import com.xiaou.auth.admin.domain.req.AdminUserReq;
import com.xiaou.auth.admin.service.AdminUserService;
import com.xiaou.common.domain.R;
import com.xiaou.log.annotation.Log;
import com.xiaou.log.enums.BusinessType;
import com.xiaou.satoken.utils.LoginHelper;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员登录管理
 */
@RestController
@RequestMapping("/admin/auth")
@Validated
public class AuthController {
    @Resource
    private LoginHelper loginHelper;
    @Resource
    private AdminUserService baseService;
    /**
     * 管理员登录
     */

    @Log(title = "登录", businessType = BusinessType.LOGIN)
    @PostMapping("/login")
    public R<SaResult> login(@RequestBody AdminUserReq req) {
        return baseService.login(req);
    }
    /**
     * 获得当前管理员信息
     */
    @GetMapping("/info")
    public R<AdminUser> getInfo(){
        String userId = loginHelper.getCurrentAppUserId();
        return R.ok(baseService.getById(userId));
    }
    /**
     * 管理员注销
     */
    @Log(title = "注销", businessType = BusinessType.LOGOUT)
    @GetMapping("/logout")
    public R<String> logout(){
        StpUtil.logout(loginHelper.getCurrentAppUserId());
        return R.ok("注销成功");
    }
    /**
     * 获得当前登录用户属于什么角色
     */
    @GetMapping("/role")
    public R<String> getRole(){
        return R.ok(StpUtil.getRoleList().toString());
    }
    /**
     * 修改密码
     */
    @Log(title = "修改密码", businessType = BusinessType.UPDATE)
    @PostMapping("/updatePassword")
    public R<String> updatePassword(@RequestParam String oldPassword, @RequestParam String newPassword){
        return baseService.updatePassword(oldPassword, newPassword);
    }
}
