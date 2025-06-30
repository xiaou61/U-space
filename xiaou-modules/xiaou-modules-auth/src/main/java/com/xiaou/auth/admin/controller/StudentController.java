package com.xiaou.auth.admin.controller;

import com.xiaou.auth.admin.domain.resp.StudentInfoPageResp;
import com.xiaou.auth.user.domain.resp.StudentInfoResp;
import com.xiaou.auth.user.service.StudentService;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台管理学生信息的控制器
 */
@RestController
@RequestMapping("/admin/student")
@Validated
public class StudentController {

    @Resource
    private StudentService studentService;
    /**
     * 分页查看所有学生信息
     */
    @PostMapping("/page")
    public R<PageRespDto<StudentInfoPageResp>> page(@RequestBody PageReqDto req){
        return studentService.pageAll(req);
    }
    /**
     * 查看所有未审核的学生信息
     */
    @GetMapping("/page/unaudited")
    public R<List<StudentInfoPageResp>> pageUnAudited(){
        return studentService.pageUnAudited();
    }
    /**
     * 审核学生
     */
    @PostMapping("/audit")
    public R<String> audit(@RequestParam String id){
        return studentService.audit(id);
    }
}
