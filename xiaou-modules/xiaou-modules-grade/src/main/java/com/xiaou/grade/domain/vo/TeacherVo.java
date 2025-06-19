package com.xiaou.grade.domain.vo;

import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaou.grade.domain.entity.Teacher;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

/**
 * @TableName u_teacher
 */
@Data
@AutoMapper(target = Teacher.class)
public class TeacherVo {
    /**
     * 教师ID
     */
    private Long id;

    /**
     * 教师姓名
     */
    private String name;

    /**
     * 手机号（用于登录）
     */
    private String phone;

    /**
     * 密码（默认为手机号后六位）
     */
    private String password;

    /**
     * 状态（1-正常，0-禁用）
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updatedAt;
}