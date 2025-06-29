package com.xiaou.auth.admin.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 班级信息表
 * @TableName u_class
 */
@TableName(value ="u_class")
@Data
public class ClassEntity {
    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 班级名称
     */
    private String className;

    /**
     * 年级，2025级
     */
    private String grade;

    /**
     * 所属专业，例如：计算机科学与技术
     */
    private String major;

    /**
     * 班主任姓名
     */
    private String classTeacher;

    /**
     * 学生人数
     */
    private Integer studentCount;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createdAt;

    /**
     * 最后更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updatedAt;


}