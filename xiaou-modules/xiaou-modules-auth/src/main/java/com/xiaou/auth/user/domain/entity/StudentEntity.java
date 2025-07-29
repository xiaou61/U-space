package com.xiaou.auth.user.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

import lombok.Data;

/**
 * 学生表
 *
 * @TableName u_student
 */
@TableName(value = "u_student")
@Data
public class StudentEntity {
    /**
     * 学生ID，主键UUID（无连字符）
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 学号（唯一编号）
     */
    private String studentNo;

    /**
     * 学生姓名
     */
    private String name;

    /**
     * 班级ID，关联class表主键
     */
    private String classId;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 密码（默认学号或手机号后6位）
     */
    private String password;

    /**
     * 创建时间
     */
    private Date createdAt;

    /**
     * 最后更新时间
     */
    private Date updatedAt;

    /**
     * 状态
     */
    private Integer status;
    /**
     * 头像
     */
    private String avatar;
}