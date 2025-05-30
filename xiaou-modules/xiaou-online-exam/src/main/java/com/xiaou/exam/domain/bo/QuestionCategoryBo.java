package com.xiaou.exam.domain.bo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xiaou.exam.domain.entity.QuestionCategory;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.util.Date;

/**
 * @TableName u_question_category
 */

@Data
@AutoMapper(target = QuestionCategory.class)
public class QuestionCategoryBo {

    /**
     * 分类名称
     */
    private String name;

    /**
     * 父分类ID，0表示一级分类
     */
    private Long parentId;

}