package com.xiaou.exam.domain.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName(value ="u_practice_question_record")
@Data
public class PracticeQuestionRecord {
    /**
     * 
     */
    @TableId
    private Long id;

    /**
     * 练习会话ID
     */
    private Long sessionId;

    /**
     * 题目ID
     */
    private Long questionId;

    /**
     * 用户答案
     */
    private String userAnswer;

    /**
     * 是否正确
     */
    private Integer isCorrect;
}