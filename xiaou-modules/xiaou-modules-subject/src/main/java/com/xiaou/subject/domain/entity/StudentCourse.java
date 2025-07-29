package com.xiaou.subject.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 学生选课记录表
 * 
 * @TableName u_student_course
 */
@TableName(value = "u_student_course")
@Data
public class StudentCourse {
    /**
     * 记录ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date selectTime;

    /**
     * 退课时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date cancelTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}