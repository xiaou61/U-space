package com.xiaou.web.controller;

import com.xiaou.common.domain.R;
import com.xiaou.common.exception.ErrorCode;
import com.xiaou.common.exception.ThrowUtils;
import com.xiaou.web.domain.User;
import com.xiaou.web.service.AdminUserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {
    @Resource
    private AdminUserService adminUserService;

    @PostMapping("/login")
    public R<User> userLogin(@RequestBody User user, HttpServletRequest request) {
        ThrowUtils.throwIf(user == null, ErrorCode.PARAMS_ERROR);
        return adminUserService.login(user, request);
    }

    @GetMapping("/get/login")
    public R<User> getLoginUser(HttpServletRequest request) {
        User loginUser = adminUserService.getLoginUser(request);
        return R.ok(loginUser);
    }

    @PostMapping("/logout")
    public R<Boolean> userLogout(HttpServletRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        boolean result = adminUserService.userLogout(request);
        return R.ok(result);
    }

}
