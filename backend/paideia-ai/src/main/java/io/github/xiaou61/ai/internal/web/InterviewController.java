package io.github.xiaou61.ai.internal.web;

import io.github.xiaou61.ai.InterviewEvaluation;
import io.github.xiaou61.ai.internal.interview.InterviewReviewService;
import io.github.xiaou61.platform.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.github.xiaou61.platform.ApiAccess;

/**
 * 面试点评端点（WORK-010 T-10/T-11）。
 *
 * <p>为什么是**普通请求/响应**而不是 SSE：点评是一次性结果（一段 JSON），没有逐步显示的需求。
 * 而落库时机必须是"拿到完整结果之后"——用 SSE 会把这件事变成第二种时序。
 *
 * <p>归属：路径里只有会话 id，用户身份一律取自令牌；不存在 / 不是本人的 / 已过期都是同一条 404
 * （由 {@code ConversationService.readMessages} 的 SQL 校验保证）。
 *
 * <p>点评**不挂任何工具**（AC-007）：工具集为空这件事由 {@code AssistantTools.schemas(REVIEW)} 保证，
 * 而这次调用根本不经过工具循环——它走的是独立的 {@code InterviewReviewer} 端口。
 */
@ApiAccess(ApiAccess.Access.AUTHENTICATED)
@RestController
@RequestMapping("/api/v1/ai/interviews")
class InterviewController {

    private final InterviewReviewService reviews;

    InterviewController(InterviewReviewService reviews) {
        this.reviews = reviews;
    }

    /**
     * 给一段面试对话生成点评并落库。
     *
     * <p>错误语义与其它 AI 端点一致：余额不足 → 402（不发起上游请求）；会话不是本人的 → 404；
     * 上游不可用或结果不可解析 → 500（**且不落库**）。
     */
    @PostMapping("/{conversationId}/review")
    ApiResponse<InterviewReviewService.EvaluationView> review(@PathVariable("conversationId") long conversationId) {
        return ApiResponse.ok(reviews.review(AiRequester.require(), conversationId));
    }

    /** 我历次面试的评估（新 → 旧）。"进度与弱项"要能查，所以有这条。 */
    @GetMapping("/evaluations")
    ApiResponse<List<InterviewReviewService.EvaluationView>> mine() {
        return ApiResponse.ok(reviews.listMine(AiRequester.require()));
    }
}
