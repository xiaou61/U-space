package com.xiaou.grade.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 
 * @TableName u_classroom
 */
@TableName(value ="u_classroom")
@Data
public class Classroom {
    /**
     * 班级ID
     */
    private Long id;

    /**
     * 班级名称
     */
    private String name;

    /**
     * 班级邀请码（扫码/手动输入用）
     */
    private String code;

    /**
     * 创建者教师ID
     */
    private Long teacherId;

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