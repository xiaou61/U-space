package com.xiaou.exam.domain.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 试题表
 *
 * @TableName u_question
 */
@TableName(value = "u_question")
@Data
public class Question {
    /**
     * 试题ID
     */
    @TableId
    private Long id;

    /**
     * 所属题库ID
     */
    private Long repoId;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 题目类型：1单选 2多选 3判断 4简答
     */
    private Integer type;

    /**
     * 题目内容
     */
    private String content;

    /**
     * 正确答案（选择题为A,B,C,D）
     */
    private String correctAnswer;

    /**
     * 难度：1简单 2中等 3困难
     */
    private Integer difficulty;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 逻辑删除：0未删除 1删除
     */
    @TableLogic
    private Integer isDeleted;
}