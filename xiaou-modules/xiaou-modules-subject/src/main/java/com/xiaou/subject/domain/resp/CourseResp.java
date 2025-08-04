package com.xiaou.subject.domain.resp;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 课程表
 * @TableName u_course
 */
@Data
public class CourseResp {
    private String id;

    /**
     * 课程编号
     */
    private String courseCode;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 授课教师姓名
     */
    private String teacherName;

    /**
     * 课程容量
     */
    private Integer capacity;

    /**
     * 已选人数
     */
    private Integer selectedCount;

    /**
     * 学分
     */
    private BigDecimal credit;

    /**
     * 课程开始时间
     */
    private Date startTime;

    /**
     * 课程结束时间
     */
    private Date endTime;

    /**
     * 上课教室
     */
    private String classroom;

    /**
     * 上课描述
     */
    private String period;

    /**
     * 课程描述
     */
    private String description;

    /**
     * 创建时间
     */
    private Date createTime;

    private Integer type;

}