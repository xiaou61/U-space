package com.xiaou.grade.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.xiaou.common.domain.R;
import com.xiaou.grade.domain.bo.TeacherBo;
import com.xiaou.grade.domain.vo.TeacherVo;
import com.xiaou.grade.service.TeacherService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/grade")
@Validated
public class GradeController {
    private static final Logger log = LoggerFactory.getLogger(GradeController.class);
    @Resource
    private TeacherService teacherService;

    /**
     * 新增教师接口
     */
    @PostMapping("/addTeacher")
    public R<TeacherVo> addTeacher(TeacherBo bo) {
        return R.ok(teacherService.addTeacher(bo));
    }

    @PostMapping("/deleteTeacher")
    public R<Void> deleteTeacher(Long id) {
        return teacherService.removeById(id) ? R.ok() : R.fail();
    }

    /**
     * 教师修改密码
     */
    @PostMapping("/updatePassword")
    public R<Void> updatePassword(String oldPassword, String newPassword) {
        return teacherService.updatePassword(oldPassword, newPassword) ? R.ok() : R.fail();
    }

    /**
     * 教师创建班级
     */
    @GetMapping("/test")
    @SaCheckRole("teacher")
    public void test() {
        log.info("权限校验测试");
        return;
    }


}
