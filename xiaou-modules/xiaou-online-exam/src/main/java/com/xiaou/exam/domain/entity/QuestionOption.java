package com.xiaou.exam.domain.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 试题选项表
 *
 * @TableName u_question_option
 */
@TableName(value = "u_question_option")
@Data
public class QuestionOption {
    /**
     * 选项ID
     */
    @TableId
    private Long id;

    /**
     * 所属试题ID
     */
    private Long questionId;

    /**
     * 选项标识（如A/B/C/D）
     */
    private String optionKey;

    /**
     * 选项内容
     */
    private String content;

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

    private String image;

    private Integer sort;
}