package com.xiaou.exam.domain.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @TableName u_question_category
 */
@Data
public class QuestionCategoryVo {
    /**
     * 分类ID
     */
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
    private Date createTime;

    private List<QuestionCategoryVo> children;
}