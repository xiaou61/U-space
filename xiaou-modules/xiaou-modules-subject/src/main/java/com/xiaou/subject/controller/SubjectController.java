package com.xiaou.subject.controller;

import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.subject.domain.req.CourseGrabReq;
import com.xiaou.subject.domain.resp.CourseResp;
import com.xiaou.subject.domain.resp.StudentCourseResp;
import com.xiaou.subject.service.CourseService;
import com.xiaou.subject.service.StudentCourseService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/subject")
@Validated
public class SubjectController {
    @Resource
    private CourseService courseService;

    @Resource
    private StudentCourseService studentCourseService;

    /**
     * 分页查看所有课程
     */
    @PostMapping("/list")
    public R<PageRespDto<CourseResp>> list(@RequestBody PageReqDto dto) {
        return courseService.listSubject(dto);
    }

    /**
     * 高并发抢课接口
     * 支持 QPS 2000+ 稳定无超选
     * 采用 Redis 原子操作 + 分布式锁 + 异步持久化
     */
    @PostMapping("/grab")
    public R<String> grabCourse(@RequestBody @Validated CourseGrabReq req) {
        return studentCourseService.grabCourse(req);
    }

    /**
     * 退课接口
     */
    @PostMapping("/drop")
    public R<String> dropCourse(@RequestParam String courseId) {
        return studentCourseService.dropCourse(courseId);
    }

    /**
     * 查看我的选课列表
     */
    @GetMapping("/my-courses")
    public R<List<StudentCourseResp>> getMySelectedCourses() {
        return studentCourseService.getMySelectedCourses();
    }

}
