package com.xiaou.subject.controller;

import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.subject.domain.req.CourseReq;
import com.xiaou.subject.domain.resp.CourseResp;
import com.xiaou.subject.service.CourseService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/subject")
public class AdminSubjectController {

    @Resource
    private CourseService courseService;
    /**
     * 创建一个课程
     */
    @PostMapping("/create")
    public R<String > create(@RequestBody CourseReq courseReq) {
        return courseService.create(courseReq);
    }
    /**
     * 更新一个课程
     */
    @PostMapping("/update")
    public R<String> update(@RequestParam String id, @RequestBody CourseReq courseReq) {
        return courseService.updateSubject(id,courseReq);
    }
    /**
     * 删除一个课程
     */
    @PostMapping("/delete")
    public R<String> delete(@RequestParam String id) {
        return courseService.deleteSubject(id);
    }
    /**
     * 分页查看所有课程
     */
    @PostMapping("/list")
    public R<PageRespDto<CourseResp>> list(@RequestBody PageReqDto dto){
        return courseService.listSubject(dto);
    }
    /**
     * 添加课程与班级关联
     */
    @PostMapping("/add-class-course")
    public R<String> addClassCourse(@RequestParam String courseId, @RequestParam String classId) {
        return courseService.addClassCourse(courseId,classId);
    }
    /**
     * 查看已经关联的班级
     */
    @GetMapping("/list-class")
    public R<List<String>> listClass(@RequestParam String courseId) {
        return courseService.listClass(courseId);
    }
}
