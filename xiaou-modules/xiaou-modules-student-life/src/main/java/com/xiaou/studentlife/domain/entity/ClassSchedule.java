package com.xiaou.studentlife.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 班级课表
 */
@TableName(value ="u_class_schedule")
@Data
public class ClassSchedule {
    /**
     * 主键ID
     */
    @TableId
    private Long id;

    /**
     * 班级ID（关联学生的所属班级）
     */
    private Long classId;

    /**
     * 课程名称
     */
    private String courseName;

    /**
     * 任课老师名称
     */
    private String teacherName;

    /**
     * 上课教室
     */
    private String classroom;

    /**
     * 星期几（1=周一，7=周日）
     */
    private Integer dayOfWeek;

    /**
     * 第几节课（如1表示第一节）
     */
    private Integer period;

    /**
     * 周次范围，如"1-16"、"1,3,5,7"
     */
    private String weekRange;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}