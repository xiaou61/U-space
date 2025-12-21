package com.xiaou.mockinterview.controller;

import com.xiaou.common.core.domain.Result;
import com.xiaou.common.satoken.StpUserUtil;
import com.xiaou.mockinterview.domain.MockInterviewSession;
import com.xiaou.mockinterview.dto.request.SubmitAnswerRequest;
import com.xiaou.mockinterview.dto.response.AnswerFeedbackResponse;
import com.xiaou.mockinterview.dto.response.InterviewQuestionResponse;
import com.xiaou.mockinterview.dto.response.InterviewReportResponse;
import com.xiaou.mockinterview.service.MockInterviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 模拟面试会话控制器
 *
 * @author xiaou
 */
@Tag(name = "模拟面试-会话", description = "面试进行中相关接口")
@RestController
@RequestMapping("/user/mock-interview/session")
@RequiredArgsConstructor
public class MockInterviewSessionController {

    private final MockInterviewService mockInterviewService;

    @Operation(summary = "开始面试（获取第一题）")
    @PostMapping("/start")
    public Result<InterviewQuestionResponse> startInterview(@RequestBody StartRequest request) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        InterviewQuestionResponse response = mockInterviewService.startInterview(userId, request.getSessionId());
        return Result.success(response);
    }

    @Operation(summary = "提交回答")
    @PostMapping("/answer")
    public Result<AnswerFeedbackResponse> submitAnswer(@RequestBody SubmitAnswerRequest request) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        AnswerFeedbackResponse response = mockInterviewService.submitAnswer(userId, request);
        return Result.success("评价完成", response);
    }

    @Operation(summary = "跳过当前题目")
    @PostMapping("/skip")
    public Result<InterviewQuestionResponse> skipQuestion(@RequestBody SkipRequest request) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        InterviewQuestionResponse response = mockInterviewService.skipQuestion(userId, request.getSessionId(), request.getQaId());
        return Result.success(response);
    }

    @Operation(summary = "获取下一题")
    @GetMapping("/next")
    public Result<InterviewQuestionResponse> getNextQuestion(@RequestParam Long sessionId) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        InterviewQuestionResponse response = mockInterviewService.getNextQuestion(userId, sessionId);
        return Result.success(response);
    }

    @Operation(summary = "结束面试")
    @PostMapping("/end")
    public Result<InterviewReportResponse> endInterview(@RequestBody EndRequest request) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        InterviewReportResponse report = mockInterviewService.endInterview(userId, request.getSessionId());
        return Result.success(report);
    }

    @Operation(summary = "获取会话状态")
    @GetMapping("/{id}/status")
    public Result<MockInterviewSession> getSessionStatus(@PathVariable Long id) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        MockInterviewSession session = mockInterviewService.getSessionStatus(userId, id);
        return Result.success(session);
    }

    @Operation(summary = "请求追问")
    @PostMapping("/follow-up")
    public Result<InterviewQuestionResponse> requestFollowUp(@RequestBody FollowUpRequest request) {
        Long userId = StpUserUtil.getLoginIdAsLong();
        InterviewQuestionResponse response = mockInterviewService.requestFollowUp(userId, request.getSessionId(), request.getQaId());
        return Result.success(response);
    }

    // =================== 内部类 ===================

    @lombok.Data
    public static class StartRequest {
        private Long sessionId;
    }

    @lombok.Data
    public static class SkipRequest {
        private Long sessionId;
        private Long qaId;
    }

    @lombok.Data
    public static class EndRequest {
        private Long sessionId;
    }

    @lombok.Data
    public static class FollowUpRequest {
        private Long sessionId;
        private Long qaId;
    }
}
