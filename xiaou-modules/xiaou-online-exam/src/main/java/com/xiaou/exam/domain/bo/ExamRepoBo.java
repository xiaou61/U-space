package com.xiaou.exam.domain.bo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xiaou.exam.domain.entity.ExamRepo;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Date;

/**
 * @TableName u_exam_repo
 */
@AutoMapper(target = ExamRepo.class)
@Data
public class ExamRepoBo {

    /**
     * 题库标题
     */
    @NotBlank(message = "题库标题不能为空")
    private String title;

    /**
     * 分类ID
     */
    @NotBlank(message = "分类ID不能为空")
    private Long categoryId;

    /**
     * 是否可以练习 默认为false
     */
    private Integer isExercise;
}