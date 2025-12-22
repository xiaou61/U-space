package com.xiaou.mockinterview.service;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.interview.domain.InterviewQuestionSet;
import com.xiaou.mockinterview.domain.MockInterviewDirection;
import com.xiaou.mockinterview.domain.MockInterviewSession;
import com.xiaou.mockinterview.dto.request.CreateInterviewRequest;
import com.xiaou.mockinterview.dto.request.InterviewHistoryRequest;
import com.xiaou.mockinterview.dto.request.SubmitAnswerRequest;
import com.xiaou.mockinterview.dto.response.*;

import java.util.List;

/**
 * 模拟面试核心服务接口
 *
 * @author xiaou
 */
public interface MockInterviewService {

    /**
     * 获取面试方向列表
     */
    List<MockInterviewDirection> getDirections();

    /**
     * 获取面试配置选项
     */
    InterviewConfigResponse getConfig();

    /**
     * 创建面试会话
     */
    InterviewSessionResponse createInterview(Long userId, CreateInterviewRequest request);

    /**
     * 开始面试（获取第一题）
     */
    InterviewQuestionResponse startInterview(Long userId, Long sessionId);

    /**
     * 提交回答
     */
    AnswerFeedbackResponse submitAnswer(Long userId, SubmitAnswerRequest request);

    /**
     * 跳过当前题目
     */
    InterviewQuestionResponse skipQuestion(Long userId, Long sessionId, Long qaId);

    /**
     * 获取下一题
     */
    InterviewQuestionResponse getNextQuestion(Long userId, Long sessionId);

    /**
     * 结束面试
     */
    InterviewReportResponse endInterview(Long userId, Long sessionId);

    /**
     * 获取会话状态
     */
    MockInterviewSession getSessionStatus(Long userId, Long sessionId);

    /**
     * 获取面试历史记录
     */
    PageResult<MockInterviewSession> getHistory(Long userId, InterviewHistoryRequest request);

    /**
     * 获取面试报告
     */
    InterviewReportResponse getReport(Long userId, Long sessionId);

    /**
     * 删除面试记录
     */
    void deleteInterview(Long userId, Long sessionId);

    /**
     * 获取用户统计数据
     */
    InterviewStatsResponse getStats(Long userId);

    /**
     * 生成面试总结（用户手动触发）
     */
    InterviewReportResponse generateSummary(Long userId, Long sessionId);

    /**
     * 获取可用题库列表
     */
    List<InterviewQuestionSet> getQuestionSets(String direction);

    /**
     * 用户请求追问
     */
    InterviewQuestionResponse requestFollowUp(Long userId, Long sessionId, Long qaId);
}
