package com.xiaou.subject.domain.mq;

import lombok.Data;

import java.util.Date;

/**
 * 抢课消息队列实体
 */
@Data
public class CourseGrabMq {
    /**
     * 学生ID
     */
    private String studentId;

    /**
     * 课程ID
     */
    private String courseId;

    /**
     * 抢课时间
     */
    private Date grabTime;

    /**
     * 操作类型：0-选课 1-退课
     */
    private Integer operation;
}