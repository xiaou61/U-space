package io.github.xiaou61.ai.internal.web;

import io.github.xiaou61.ai.internal.billing.CreditService;
import io.github.xiaou61.platform.ApiAccess;
import io.github.xiaou61.platform.ApiResponse;
import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 本人的积分余额与消耗明细（WORK-009 REQ-008 / AC-010）。
 *
 * <p>**不返回别人的任何东西**：查询条件里带的是当前登录者的 id，且用的是"按 user_id 查流水"
 * 那条 SQL——没有"看别人明细"的入口，不是靠调用方自觉。
 *
 * <p>只给 {@code 201 + 200} 这类读者：不提供充值（本项不做充值），也不提供任何写操作。
 */
@ApiAccess(ApiAccess.Access.AUTHENTICATED)
@RestController
@RequestMapping("/api/v1/ai/credits")
class AiCreditController {

    private final CreditService credits;

    AiCreditController(CreditService credits) {
        this.credits = credits;
    }

    /**
     * 明细行。
     *
     * <p>刻意**不含 {@code operatorUserId} 与 {@code userId}**：这两样是管理端审计用的，
     * 放到用户自己的界面里只会多一处要解释的字段。
     */
    record CreditRow(Instant createdAt, long delta, String kind, String model,
                     Integer inputTokens, Integer outputTokens, Long refMessageId, String note) {

        static CreditRow of(CreditService.LedgerRow row) {
            return new CreditRow(row.createdAt(), row.delta(), row.kind(), row.model(),
                    row.inputTokens(), row.outputTokens(), row.refMessageId(), row.note());
        }
    }

    record BalanceView(long balance, List<CreditRow> ledger) {
    }

    @GetMapping
    ApiResponse<BalanceView> get() {
        long userId = AiRequester.require();
        return ApiResponse.ok(new BalanceView(
                credits.balance(userId),
                credits.listForUser(userId, 50).stream().map(CreditRow::of).toList()));
    }
}
