package com.xiaou.user.controller;

import cn.dev33.satoken.util.SaResult;
import com.xiaou.common.domain.R;
import com.xiaou.user.domain.bo.StudentUserBo;
import com.xiaou.user.domain.entity.StudentUser;
import com.xiaou.user.domain.vo.StudentUserVo;
import com.xiaou.user.service.StudentUserService;
import com.xiaou.utils.LoginHelper;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/student")
@Validated
public class StudentUserController {
    @Resource
    private StudentUserService studentUserService;

    @PostMapping("/login")
    public R<SaResult> login(@RequestBody @Validated StudentUserBo bo) {
        return studentUserService.login(bo);
    }


    /**
     * 获得当前登录学生信息
     */
    @GetMapping("/get/login")
    public R<StudentUserVo> getLogin() {
        String currentStudentId = LoginHelper.getCurrentStudentId();
        return studentUserService.getLogin(currentStudentId);
    }

}
