package com.xiaou.exam.domain.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.xiaou.exam.domain.entity.ExamRepo;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.util.Date;

/**
 * @TableName u_exam_repo
 */

@Data
@AutoMapper(target = ExamRepo.class)
public class ExamRepoVo {
    /**
     * id   题库表
     */
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
     * 创建时间
     */
    private Date createTime;

    /**
     * 是否可以练习 默认为false
     */
    private Integer isExercise;

    // 分类ID
    private Long categoryId;
    // 分类名称
    private String categoryName;
    // 题目数量
    private Integer questionCount;
}