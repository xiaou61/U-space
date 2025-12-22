package com.xiaou.mockinterview.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 面试问题响应DTO
 *
 * @author xiaou
 */
@Data
public class InterviewQuestionResponse {

    /**
     * 问答记录ID
     */
    private Long qaId;

    /**
     * 题目序号
     */
    private Integer questionOrder;

    /**
     * 总题目数
     */
    private Integer totalQuestions;

    /**
     * 问题内容
     */
    private String questionContent;

    /**
     * 问题类型：1-主问题 2-追问
     */
    private Integer questionType;

    /**
     * 考察知识点
     */
    private List<String> knowledgePoints;

    /**
     * 预计回答时间（秒）
     */
    private Integer estimatedTime;

    /**
     * 是否已完成
     */
    private Boolean finished;
}
