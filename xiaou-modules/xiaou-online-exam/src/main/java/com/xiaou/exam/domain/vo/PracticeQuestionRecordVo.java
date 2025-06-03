package com.xiaou.exam.domain.vo;

import lombok.Data;

@Data

public class PracticeQuestionRecordVo {
    private Long questionId;
    private String questionContent;
    private String correctAnswer;
    private Integer type;      // 题目类型：1单选 2多选 3判断 4简答
    private String userAnswer;
    private Boolean isCorrect;
}

