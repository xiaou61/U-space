package com.xiaou.subject.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.subject.domain.entity.Course;
import com.xiaou.subject.domain.req.CourseReq;
import com.xiaou.subject.domain.resp.CourseResp;

import java.util.List;

public interface CourseService extends IService<Course> {

    R<String> create(CourseReq courseReq);

    R<String> updateSubject(String id, CourseReq courseReq);

    R<String> deleteSubject(String id);

    R<PageRespDto<CourseResp>> listSubject(PageReqDto dto);

    R<String> addClassCourse(String courseId, String classId);

    R<List<String>> listClass(String courseId);
}
