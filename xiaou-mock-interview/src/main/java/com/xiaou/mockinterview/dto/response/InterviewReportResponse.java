package com.xiaou.mockinterview.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 面试报告响应DTO
 *
 * @author xiaou
 */
@Data
public class InterviewReportResponse {

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 面试方向代码
     */
    private String direction;

    /**
     * 面试方向名称
     */
    private String directionName;

    /**
     * 难度级别
     */
    private Integer level;

    /**
     * 难度级别名称
     */
    private String levelName;

    /**
     * 总分（满分100）
     */
    private Integer totalScore;

    /**
     * 各维度得分
     */
    private Dimensions dimensions;

    /**
     * AI总评
     */
    private String aiSummary;

    /**
     * AI改进建议
     */
    private List<String> aiSuggestion;

    /**
     * 薄弱知识点
     */
    private List<String> weakPoints;

    /**
     * 问答详情列表
     */
    private List<QADetail> qaList;

    /**
     * 面试时长（秒）
     */
    private Integer duration;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime endTime;

    /**
     * 获得积分
     */
    private Integer pointsEarned;

    /**
     * 各维度得分
     */
    @Data
    public static class Dimensions {
        /**
         * 知识掌握度
         */
        private Integer knowledge;

        /**
         * 深度理解
         */
        private Integer depth;

        /**
         * 表达能力
         */
        private Integer expression;

        /**
         * 应变能力
         */
        private Integer adaptability;
    }

    /**
     * 问答详情
     */
    @Data
    public static class QADetail {
        /**
         * 问答记录ID
         */
        private Long qaId;

        /**
         * 题目序号
         */
        private Integer questionOrder;

        /**
         * 问题内容
         */
        private String questionContent;

        /**
         * 用户回答
         */
        private String userAnswer;

        /**
         * 得分
         */
        private Integer score;

        /**
         * 反馈信息
         */
        private AnswerFeedbackResponse.Feedback feedback;

        /**
         * 参考答案
         */
        private String referenceAnswer;

        /**
         * 追问列表
         */
        private List<QADetail> followUps;
    }
}
