package com.xiaou.auth.admin.domain.resp;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaou.auth.admin.domain.entity.ClassEntity;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.util.Date;

/**
 * 班级信息表
 * @TableName u_class
 */
@Data
@AutoMapper(target = ClassEntity.class)
public class ClassResp {

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