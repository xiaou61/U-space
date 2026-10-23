package io.github.xiaou61.ai.internal.web;

import io.github.xiaou61.ai.internal.feedback.AiFeedbackService;
import io.github.xiaou61.platform.ApiAccess;
import io.github.xiaou61.platform.ApiResponse;
import io.github.xiaou61.platform.PageQuery;
import io.github.xiaou61.platform.PageResult;
import java.time.Instant;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 助手回答反馈的管理端列表（WORK-036 C 组）。
 *
 * <p><b>为什么单独一个控制器</b>：访问级别只能声明在一处——方法级与类级同时声明时，
 * "这个端点到底谁说了算"要读两处才能确定，{@code ApiSurfaceTest} 会把它判成失败。
 * 提交端点是 AUTHENTICATED（学习者要用），这里是 ADMIN，两者不能共用一个类级声明。
 *
 * <p>路径走 {@code /ai/feedback/admin/**}，由 {@code SecurityConfiguration} 里那条
 * {@code hasRole("ADMIN")} 兜住，且**必须排在 {@code /api/v1/ai/**} 之前**。
 */
@ApiAccess(ApiAccess.Access.ADMIN)
@RestController
@RequestMapping("/api/v1/ai/feedback")
class AiFeedbackAdminController {

    private final AiFeedbackService service;

    AiFeedbackAdminController(AiFeedbackService service) {
        this.service = service;
    }

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
}
