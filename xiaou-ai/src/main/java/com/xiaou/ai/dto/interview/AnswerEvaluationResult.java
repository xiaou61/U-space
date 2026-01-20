package com.xiaou.ai.dto.interview;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * AI答案评价结果
 *
 * @author xiaou
 */
@Data
@Accessors(chain = true)
public class AnswerEvaluationResult {

    /**
     * 评分（0-10）
     */
    private Integer score;

    /**
     * 反馈信息
     */
    private Feedback feedback;

    /**
     * 下一步动作：followUp（追问）或 nextQuestion（下一题）
     */
    private String nextAction;

    /**
     * 追问内容（仅当nextAction为followUp时有效）
     */
    private String followUpQuestion;

    /**
     * 考察的知识点
     */
    private List<String> referencePoints;

    /**
     * 是否为降级结果
     */
    private boolean fallback;

    /**
     * 反馈详情
     */
    @Data
    @Accessors(chain = true)
    public static class Feedback {
        /**
         * 优点
         */
        private List<String> strengths;

        /**
         * 需要改进的地方
         */
        private List<String> improvements;
    }

    /**
     * 创建降级结果
     */
    public static AnswerEvaluationResult fallbackResult(String answer, int followUpCount) {
        AnswerEvaluationResult result = new AnswerEvaluationResult();
        
        // 根据回答长度简单评分
        int score = 5;
        if (answer != null) {
            int length = answer.length();
            if (length > 200) {
                score = 7;
            } else if (length > 100) {
                score = 6;
            } else if (length < 20) {
                score = 3;
            }
        }
        result.setScore(score);

        Feedback feedback = new Feedback();
        feedback.setStrengths(List.of("感谢您的回答"));
        feedback.setImprovements(List.of("建议补充更多细节"));
        result.setFeedback(feedback);

        result.setNextAction("nextQuestion");
        result.setReferencePoints(List.of());
        result.setFallback(true);

        return result;
    }
}
