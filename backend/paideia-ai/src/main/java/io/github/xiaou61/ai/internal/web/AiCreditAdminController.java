package io.github.xiaou61.ai.internal.web;

import io.github.xiaou61.ai.internal.billing.CreditService;
import io.github.xiaou61.platform.ApiResponse;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 积分的管理端接口（WORK-009 REQ-004 / REQ-007）。
 *
 * <p>**鉴权靠路径规则**：整个 {@code /api/v1/ai/admin/**} 由
 * {@code SecurityConfiguration} 里的 {@code hasRole("ADMIN")} 兜住，且那条规则必须排在
 * 更宽的 {@code /api/v1/ai/**} 之前（写反了不会报错，只会静默失效）。所以本类里
 * **不逐方法加注解**——与本项目其它管理端控制器一致。
 *
 * <p>**本期没有充值**（用户决定 3）：积分唯一的入口就是这里的调整。所以"给谁发多少分"是
 * 一个必须留下操作人与原因的写操作——它就是本项的"充值"。
 */
@RestController
@RequestMapping("/api/v1/ai/admin/credits")
class AiCreditAdminController {

    private final CreditService credits;

    AiCreditAdminController(CreditService credits) {
        this.credits = credits;
    }

    /** 调整入参。{@code delta} 正为发放、负为回收；{@code note} 是原因，审计页要显示它。 */
    record AdjustRequest(long delta, String note) {
    }

    /** 全平台最近的用量与调整流水（AC-005 / AC-006 的查询面）。 */
    @GetMapping("/audit")
    ApiResponse<List<CreditService.LedgerRow>> audit(@RequestParam(name = "limit", defaultValue = "100") int limit) {
        return ApiResponse.ok(credits.auditRecent(limit));
    }

    /** 某个用户的余额。 */
    @GetMapping("/{userId}")
    ApiResponse<Map<String, Object>> balance(@PathVariable("userId") long userId) {
        return ApiResponse.ok(Map.of("userId", userId, "balance", credits.balance(userId)));
    }

    /** 给某个用户增减积分。操作人与原因都进流水（AC-005）。 */
    @PostMapping("/{userId}/adjust")
    ApiResponse<Map<String, Object>> adjust(@PathVariable("userId") long userId,
                                            @RequestBody AdjustRequest request) {
        long operator = AiRequester.require();
        long balance = credits.adjust(userId, request.delta(), operator, request.note());
        return ApiResponse.ok(Map.of("userId", userId, "balance", balance));
    }
}
