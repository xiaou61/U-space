package com.xiaou.auth.admin.controller;

import com.xiaou.auth.admin.domain.entity.Teacher;
import com.xiaou.auth.admin.domain.req.TeacherLoginReq;
import com.xiaou.auth.admin.domain.req.TeacherReq;
import com.xiaou.auth.admin.domain.resp.TeacherResp;
import com.xiaou.auth.admin.service.TeacherService;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/teacher")
@Validated
public class TeacherController {
    @Resource
    private TeacherService teacherService;
    /**
     * 添加教师
     */
    @PostMapping("/add")
    public R<String> add(@RequestBody TeacherReq req) {
        return teacherService.add(req);
    }
    /**
     * 删除教师
     */
    @DeleteMapping("/delete")
    public R<String> delete(@RequestParam String id) {
        return teacherService.delete(id);
    }
    /**
     * 分页查看教师
     */
    @PostMapping("/page")
    public R<PageRespDto<TeacherResp>> page(@RequestBody PageReqDto req) {
        return teacherService.pageTeacher(req);
    }

}
