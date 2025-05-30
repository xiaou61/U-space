package com.xiaou.exam.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * @TableName u_question_category
 */
@TableName(value = "u_question_category")
@Data
public class QuestionCategory {
    /**
     * 分类ID
     */
    @TableId
    private Long id;

    /**
     * 分类名称
     */
    private String name;

    /**
     * 父分类ID，0表示一级分类
     */
    private Long parentId;

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
}