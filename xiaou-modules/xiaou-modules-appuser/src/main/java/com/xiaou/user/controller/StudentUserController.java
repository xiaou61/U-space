package com.xiaou.user.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.xiaou.common.domain.R;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.ratelimiter.annotation.RateLimiter;
import com.xiaou.user.domain.bo.StudentUserBo;
import com.xiaou.user.domain.entity.StudentUser;
import com.xiaou.user.domain.vo.StudentUserVo;
import com.xiaou.user.service.StudentUserService;
import com.xiaou.utils.LoginHelper;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/student")
@Validated
public class StudentUserController {
    @Resource
    private StudentUserService studentUserService;

    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody @Validated StudentUserBo bo) {
        return studentUserService.login(bo);
    }


    @GetMapping("/user/info")
    public R<StudentUserVo> getLoginInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        StudentUser user = studentUserService.getById(userId);
        if (user == null) {
            return R.fail("用户不存在");
        }
        StudentUserVo vo = MapstructUtils.convert(user, StudentUserVo.class);
        return R.ok(vo);
    }

    /**
     * 上传头像
     */
    @PostMapping("/upload/avatar")
    public R<String> uploadAvatar(@RequestParam String avatar) {
        return studentUserService.uploadAvatar(avatar);
    }

    /**
     * 用户修改密码
     */
    @PostMapping("/reset/password")
    public R<String> resetPassword(@RequestParam String password) {
        return studentUserService.resetPassword(password);
    }

    /**
     * 用户绑定邮箱
     */
    @PostMapping("/bind/email")
    @RateLimiter(key = "#email", time = 60, count = 10)
    public R<String> bindEmail(@RequestParam String email, String code) {
        return studentUserService.bindEmail(email, code);
    }


}
