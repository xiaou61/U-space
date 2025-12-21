package com.xiaou.mockinterview.dto;

import lombok.Data;

import java.util.List;

/**
 * AI评价结果DTO
 *
 * @author xiaou
 */
@Data
public class AIEvaluationResult {

    /**
     * 评分（0-10）
     */
    private Integer score;

    /**
     * 反馈信息
     */
    private Feedback feedback;

    /**
     * 下一步动作：followUp-追问 / nextQuestion-下一题
     */
    private String nextAction;

    /**
     * 追问内容（如果需要追问）
     */
    private String followUpQuestion;

    /**
     * 考察知识点
     */
    private List<String> referencePoints;

    /**
     * 反馈内容
     */
    @Data
    public static class Feedback {
        /**
         * 优点
         */
        private List<String> strengths;

        /**
         * 改进点
         */
        private List<String> improvements;
    }
}
