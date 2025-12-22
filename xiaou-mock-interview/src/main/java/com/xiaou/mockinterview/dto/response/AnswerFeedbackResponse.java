package com.xiaou.mockinterview.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 回答反馈响应DTO
 *
 * @author xiaou
 */
@Data
public class AnswerFeedbackResponse {

    /**
     * 问答记录ID
     */
    private Long qaId;

    /**
     * 得分（0-10）
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
     * 追问信息（如果需要追问）
     */
    private FollowUpQuestion followUpQuestion;

    /**
     * 是否还有下一题
     */
    private Boolean hasNext;

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

    /**
     * 追问信息
     */
    @Data
    public static class FollowUpQuestion {
        /**
         * 问答记录ID
         */
        private Long qaId;

        /**
         * 追问内容
         */
        private String questionContent;

        /**
         * 问题类型
         */
        private Integer questionType;
    }
}
