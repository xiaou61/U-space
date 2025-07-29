package com.xiaou.subject.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.common.domain.R;
import com.xiaou.subject.domain.entity.StudentCourse;
import com.xiaou.subject.domain.req.CourseGrabReq;
import com.xiaou.subject.domain.resp.StudentCourseResp;

import java.util.List;

public interface StudentCourseService extends IService<StudentCourse> {

    /**
     * 高并发抢课
     */
    R<String> grabCourse(CourseGrabReq req);

    /**
     * 退课
     */
    R<String> dropCourse(String courseId);

    /**
     * 查询学生已选课程
     */
    R<List<StudentCourseResp>> getMySelectedCourses();


    /**
     * 异步处理选课/退课操作
     */
    void asyncProcessCourseOperation(String studentId, String courseId, Integer operation);
}