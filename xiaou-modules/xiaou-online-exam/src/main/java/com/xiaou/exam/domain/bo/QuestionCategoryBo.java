package com.xiaou.exam.domain.bo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xiaou.exam.domain.entity.QuestionCategory;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
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
    @NotBlank(message = "分类名称不能为空")
    private String name;

    /**
     * 父分类ID，0表示一级分类
     */
    @NotBlank(message = "父分类ID不能为空")
    private Long parentId;

}