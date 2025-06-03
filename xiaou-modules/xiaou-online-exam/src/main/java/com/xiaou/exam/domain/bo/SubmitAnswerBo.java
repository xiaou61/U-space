package com.xiaou.exam.domain.bo;

import com.xiaou.exam.domain.entity.PracticeQuestionRecord;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data

public class SubmitAnswerBo {

    @NotNull(message = "练习会话ID不能为空")
    private Long sessionId;

    @NotNull(message = "题目ID不能为空")
    private Long questionId;

    @NotNull(message = "用户答案不能为空")
    private String userAnswer;
}
