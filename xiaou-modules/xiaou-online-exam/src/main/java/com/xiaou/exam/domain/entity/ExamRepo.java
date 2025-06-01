package com.xiaou.exam.domain.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * @TableName u_exam_repo
 */
@TableName(value = "u_exam_repo")
@Data
public class ExamRepo {
    /**
     * id   题库表
     */
    @TableId
    private Long id;

    /**
     * 创建人id
     */
    private Long userId;

    /**
     * 题库标题
     */
    private String title;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 逻辑删除：0代表未删除，1代表删除
     */
    @TableLogic
    private Integer isDeleted;

    /**
     * 是否可以练习 默认为false
     */
    private Integer isExercise;

    private Integer questionCount;
}