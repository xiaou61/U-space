package com.xiaou.subject.domain.req;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaou.subject.domain.entity.Course;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 课程表
 * 
 * @TableName u_course
 */
@Data
public class CourseReq {
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    /**
     * 课程结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
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
     * 1为必修课 2为选修课
     */
    private Integer type;

}