package com.xiaou.exam.domain.bo;

import com.xiaou.exam.domain.entity.Question;
import com.xiaou.exam.domain.entity.QuestionOption;
import io.github.linpeilie.annotations.AutoMapper;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@AutoMapper(target = Question.class)
public class QuestionBo {

    /**
     * 所属题库ID
     */
    @NotNull(message = "所属题库ID(repoId)不能为空")
    private Long repoId;


    /**
     * 试题类型
     */
    @NotNull(message = "试题类型(quType)不能为空")
    @Min(value = 1, message = "试题类型(quType)只能是：1单选2多选3判断4简答")
    @Max(value = 4, message = "试题类型(quType)只能是：1单选2多选3判断4简答")
    private Integer type;

    /**
     * 题目内容
     */
    @NotNull(message = "题目内容(content)不能为空")
    private String content;

    /**
     * 正确答案（选择题为A,B,C,D）
     */
    @NotNull(message = "正确答案(correctAnswer)不能为空")
    private String correctAnswer;

    /**
     * 难度：1简单 2中等 3困难
     */
    @NotNull(message = "难度(difficulty)不能为空")
    private Integer difficulty;

    /**
     * 选项列表
     */
    @NotNull(message = "选项列表(options)不能为空")
    private List<QuestionOption> options;
}
