package com.xiaou.exam.domain.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaou.exam.domain.entity.Question;
import com.xiaou.exam.domain.entity.QuestionOption;
import io.github.linpeilie.annotations.AutoMapper;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 试题表
 *
 * @TableName u_question
 */
@Data
@AutoMapper(target = Question.class)
public class QuestionVo {
    /**
     * 试题ID
     */
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
    private Date createTime;

    private List<QuestionOption> options;
}