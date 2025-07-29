package com.xiaou.subject.domain.resp;

import com.xiaou.subject.domain.entity.Course;
import com.xiaou.subject.domain.entity.StudentCourse;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.util.Date;

/**
 * 学生选课记录响应类
 */
@Data
@AutoMapper(target = StudentCourse.class)
public class StudentCourseResp {
    /**
     * 记录ID
     */
    private String id;

    /**
     * 学生ID
     */
    private String studentId;

    /**
     * 课程ID
     */
    private String courseId;

    /**
     * 选课状态 0-已选 1-已退课
     */
    private Integer status;

    /**
     * 选课时间
     */
    private Date selectTime;

    /**
     * 退课时间
     */
    private Date cancelTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 课程详细信息
     */
    private Course courseInfo;
}