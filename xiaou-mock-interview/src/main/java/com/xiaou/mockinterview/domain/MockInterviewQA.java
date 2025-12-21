package com.xiaou.mockinterview.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 模拟面试问答记录实体
 *
 * @author xiaou
 */
@Data
@Accessors(chain = true)
public class MockInterviewQA {

    /**
     * 记录ID
     */
    private Long id;

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 关联的题库题目ID（可为空）
     */
    private Long questionId;

    /**
     * 题目序号
     */
    private Integer questionOrder;

    /**
     * 问题内容
     */
    private String questionContent;

    /**
     * 问题类型：1-主问题 2-追问
     */
    private Integer questionType;

    /**
     * 父问答ID（追问时关联主问题）
     */
    private Long parentQaId;

    /**
     * 用户回答
     */
    private String userAnswer;

    /**
     * 回答用时（秒）
     */
    private Integer answerDurationSeconds;

    /**
     * 得分（0-10）
     */
    private Integer score;

    /**
     * AI反馈（JSON格式）
     */
    private String aiFeedback;

    /**
     * 参考答案
     */
    private String referenceAnswer;

    /**
     * 考察知识点（逗号分隔）
     */
    private String knowledgePoints;

    /**
     * 状态：0-待回答 1-已回答 2-已跳过
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;

    // =================== 查询时使用的非数据库字段 ===================

    /**
     * 追问列表（查询时使用）
     */
    private List<MockInterviewQA> followUps;
}
