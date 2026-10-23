package io.github.xiaou61.ai.internal.web;

import io.github.xiaou61.ai.internal.feedback.AiFeedbackService;
import io.github.xiaou61.platform.ApiAccess;
import io.github.xiaou61.platform.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 助手回答的反馈（WORK-036 C 组）。
 *
 * <p><b>提交端点在 {@code /api/v1/ai/feedback}（authenticated），不在 {@code /admin/**}</b>：
 * 学习者要用。用户 id 从令牌取，**请求体里不接受 userId**——与记忆控制面同一纪律，
 * 传不了别人的评价。
 *
 * <p>读取端点在 {@link AiFeedbackAdminController}，**单独一个控制器**：访问级别只能声明在
 * 一处（{@code ApiSurfaceTest} 会拦），而它要的是 ADMIN、这个控制器是 AUTHENTICATED。
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

    record SubmitRequest(Long conversationId, Long messageId, String traceId,
                         String kind, String rating, String reason) {
    }
}
