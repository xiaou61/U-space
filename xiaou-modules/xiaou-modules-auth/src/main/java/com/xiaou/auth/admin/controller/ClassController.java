package com.xiaou.auth.admin.controller;

import com.xiaou.auth.admin.domain.entity.ClassEntity;
import com.xiaou.auth.admin.domain.req.ClassReq;
import com.xiaou.auth.admin.domain.resp.ClassResp;
import com.xiaou.auth.admin.service.ClassService;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 班级管理
 */
@RestController
@RequestMapping("/admin/class")
@Validated
public class ClassController {
    @Resource
    private ClassService classService;
    /**
     * 添加班级
     */
    @PostMapping("/add")
    public R<ClassResp> add(@RequestBody ClassReq req) {
        return classService.add(req);
    }
    /**
     * 删除班级
     */
    @DeleteMapping("/delete")
    public R<String> delete(@RequestParam String id) {
        return classService.delete(id);
    }
    /**
     * 修改班级根据id
     */
    @PutMapping("/update")
    public R<ClassResp> update(@RequestBody ClassReq req,@RequestParam String id) {
        return classService.updateClass(req,id);
    }
    /**
     * 分页查看班级
     */
    @PostMapping("/page")
    public R<PageRespDto<ClassResp>> page(@RequestBody PageReqDto req) {
        return classService.pageClass(req);
    }


}
