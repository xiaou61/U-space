package io.github.xiaou61.ai.internal.web;

import io.github.xiaou61.ai.internal.telemetry.AiRunAdminService;
import io.github.xiaou61.platform.ApiAccess;
import io.github.xiaou61.platform.ApiResponse;
import io.github.xiaou61.platform.PageQuery;
import io.github.xiaou61.platform.PageResult;
import java.time.Instant;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 调用统计的管理端接口（WORK-022 REQ-008/009/010/014）。
 *
 * <p>整个 {@code /api/v1/ai/admin/**} 由 `SecurityConfiguration` 的 `hasRole("ADMIN")`
 * 兜住，且那条规则排在更宽的 {@code /api/v1/ai/**} 之前。所以本类不逐方法加注解——
 * 与 `AiCreditAdminController` 一致。
 *
 * <p>**只读**：这三个端点没有写操作。统计系统自己不该成为第二个可以被写坏的地方。
 */
@ApiAccess(ApiAccess.Access.ADMIN)
@RestController
@RequestMapping("/api/v1/ai/admin/runs")
class AiRunAdminController {

    private final AiRunAdminService service;

    AiRunAdminController(AiRunAdminService service) {
        this.service = service;
    }

    /** 概览：调用量、成功率、分位耗时、token、扣分、分布与排行。 */
    @GetMapping("/overview")
    ApiResponse<AiRunAdminService.Overview> overview(
            @RequestParam(name = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(name = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return ApiResponse.ok(service.overview(from, to));
    }

    /** 明细列表：按时间窗 + 场景 + 状态 + 用户 + 模型筛选，分页。 */
    @GetMapping
    ApiResponse<PageResult<AiRunAdminService.RunSummary>> page(
            @RequestParam(name = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(name = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(name = "kind", required = false) String kind,
            @RequestParam(name = "status", required = false) String status,
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "model", required = false) String model,
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "size", required = false) Integer size) {
        return ApiResponse.ok(service.page(from, to, kind, status, userId, model,
                PageQuery.of(page, size, null)));
    }

    /** 筛选下拉的可选值。 */
    @GetMapping("/meta")
    ApiResponse<Map<String, Object>> meta(
            @RequestParam(name = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(name = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return ApiResponse.ok(service.meta(from, to));
    }

    /** 单轮详情 + 步骤表（按 seq）。 */
    @GetMapping("/{traceId}")
    ApiResponse<AiRunAdminService.RunDetail> detail(@PathVariable("traceId") String traceId) {
        return ApiResponse.ok(service.detail(traceId));
    }
}
