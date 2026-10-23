package io.github.xiaou61.ai.internal.web;

import io.github.xiaou61.ai.internal.feedback.AiFeedbackService;
import io.github.xiaou61.platform.ApiAccess;
import io.github.xiaou61.platform.ApiResponse;
import io.github.xiaou61.platform.PageQuery;
import io.github.xiaou61.platform.PageResult;
import java.time.Instant;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 助手回答的反馈（WORK-036 C 组）。
 *
 * <p><b>提交端点在 {@code /api/v1/ai/feedback}（authenticated），不在 {@code /admin/**}</b>：
 * 学习者要用。用户 id 从令牌取，**请求体里不接受 userId**——与记忆控制面同一纪律，
 * 传不了别人的评价。
 *
 * <p>读取端点在 {@code /admin/feedback}，由既有的 {@code hasRole("ADMIN")} 兜住。
 */
@ApiAccess(ApiAccess.Access.AUTHENTICATED)
@RestController
@RequestMapping("/api/v1/ai/feedback")
class AiFeedbackController {

    private final AiFeedbackService service;

    AiFeedbackController(AiFeedbackService service) {
        this.service = service;
    }

    /** 提交。`reason` 只在 `rating=down` 时必填；`traceId` 可空（采集关闭时没有）。 */
    @PostMapping
    ApiResponse<Void> submit(@RequestBody(required = false) SubmitRequest request) {
        long userId = AiRequester.require();
        SubmitRequest body = request == null ? new SubmitRequest(null, null, null, null, null, null) : request;
        service.submit(userId, body.conversationId(), body.messageId(), body.traceId(),
                body.kind(), body.rating(), body.reason());
        return ApiResponse.ok(null);
    }

    /** 管理端列表。**走 {@code /feedback/admin/**}**：与知识库/速递同一约定（`<模块>/admin`）。 */
    @GetMapping("/admin")
    ApiResponse<PageResult<AiFeedbackService.FeedbackView>> page(
            @RequestParam(name = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(name = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(name = "rating", required = false) String rating,
            @RequestParam(name = "reason", required = false) String reason,
            @RequestParam(name = "kind", required = false) String kind,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) {
        return ApiResponse.ok(service.page(from, to, rating, reason, kind,
                PageQuery.of(page, size, null)));
    }

    record SubmitRequest(Long conversationId, Long messageId, String traceId,
                         String kind, String rating, String reason) {
    }
}
