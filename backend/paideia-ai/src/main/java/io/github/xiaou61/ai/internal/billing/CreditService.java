package io.github.xiaou61.ai.internal.billing;

import io.github.xiaou61.platform.BizException;
import io.github.xiaou61.platform.ErrorCode;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 积分的预扣、结算与调整（WORK-009）。
 *
 * <p><b>为什么是"先预留、后结算"而不是"调完再扣"</b>：纯后扣在并发下必然超扣——多个请求各自
 * 读到"余额够"，最后一个把余额扣成负数。预留用的是一条带 {@code balance >= ?} 的原子 UPDATE，
 * 影响行数就是判据（AC-004）。
 *
 * <p><b>扣减失败一律不发起上游请求</b>（REQ-006）：预留在调用之前同步完成，失败就直接抛
 * {@link ErrorCode#INSUFFICIENT_CREDITS}，界面据此给可操作提示。
 *
 * <p><b>单次调用有硬上限</b>（REQ-010）：结算时用量折算出的积分会被截到
 * {@code maxCreditsPerCall}。它同时封住了"预留估少了"时的超额扣减。
 *
 * <p><b>账面永远不为负</b>：所有扣减都走带条件的 UPDATE；真的扣不动时**不扣**，把差额写进
 * 流水备注留痕（供人工对账），而不是让余额变负或抛错把已经产生的回答吞掉。
 */
@Service
public class CreditService {

    private static final Logger log = LoggerFactory.getLogger(CreditService.class);

    /** 明细/审计一次最多返回多少行。量级是"调用次数"，先给足；真不够再做分页。 */
    private static final int LIST_LIMIT = 200;

    private final CreditAccountMapper accounts;
    private final CreditLedgerMapper ledger;
    private final BillingProperties billing;

    CreditService(CreditAccountMapper accounts, CreditLedgerMapper ledger, BillingProperties billing) {
        this.accounts = accounts;
        this.ledger = ledger;
        this.billing = billing;
    }

    /** 对外的流水行。 */
    public record LedgerRow(long id, long userId, long delta, String kind, String model,
                            Integer inputTokens, Integer outputTokens, Long refMessageId,
                            Long operatorUserId, String note, Instant createdAt) {

        static LedgerRow of(CreditLedger row) {
            return new LedgerRow(row.getId(), row.getUserId(), row.getDelta(), row.getKind(), row.getModel(),
                    row.getInputTokens(), row.getOutputTokens(), row.getRefMessageId(),
                    row.getOperatorUserId(), row.getNote(), row.getCreatedAt());
        }
    }

    public boolean enabled() {
        return billing.isEnabled();
    }

    public long balance(long userId) {
        Long balance = accounts.findBalance(userId);
        return balance == null ? 0 : balance;
    }

    /**
     * 一次调用的预留额。
     *
     * <p>ponytail: 不引入 tokenizer 去精确预算——那是一个新依赖，而预留只为"并发时不超扣"服务。
     * 代价是余额会被短暂多占（毫秒级，结算立即归还）。真出现"并发下余额被占住导致误拒"的实测，
     * 再改成"按上一次同类调用的用量滑动平均"。
     */
    public long estimate() {
        return Math.max(1, Math.round(billing.getMaxCreditsPerCall() * billing.getReserveFactor()));
    }

    /**
     * 预留：从余额里先扣掉预留额。
     *
     * @return 实际预留的额度（调用方要把它带回来做结算）
     * @throws BizException {@link ErrorCode#INSUFFICIENT_CREDITS} 余额不足；**此时尚未发起任何上游请求**
     */
    @Transactional
    public long reserve(long userId, long amount) {
        if (!billing.isEnabled()) {
            return 0;
        }
        if (amount <= 0) {
            return 0;
        }
        if (accounts.deductIfEnough(userId, amount) == 0) {
            throw new BizException(ErrorCode.INSUFFICIENT_CREDITS,
                    "积分不足，本次需要预留 " + amount + " 分（当前 " + balance(userId) + " 分）。"
                            + "当前没有自动充值，请联系管理员。");
        }
        write(userId, -amount, CreditLedger.KIND_RESERVE, null, null, null, null, null, null);
        return amount;
    }

    /**
     * 结算：按**实际用量**算出该扣多少，与预留的差额补退。
     *
     * <p>三步各自的意义：
     * <ol>
     *   <li>折算并**截到单次上限**（REQ-010）；</li>
     *   <li>实际 ≤ 预留 → 退还差额（这是常态）；</li>
     *   <li>实际 &gt; 预留 → 补扣差额；补不动（余额不够）就**不扣**，差额写进备注留痕。</li>
     * </ol>
     *
     * <p>用量为 {@code null}（上游没给）时按 0 计——**退还全部预留**：没有依据就不该扣。
     */
    @Transactional
    public void settle(long userId, long reserved, String model, Integer inputTokens, Integer outputTokens,
                       Long refMessageId) {
        if (!billing.isEnabled() || reserved <= 0) {
            return;
        }
        long actual = Math.min(
                billing.creditsFor(model, orZero(inputTokens), orZero(outputTokens)),
                billing.getMaxCreditsPerCall());
        if (actual >= reserved) {
            long excess = actual - reserved;
            if (excess > 0 && accounts.deductIfEnough(userId, excess) == 1) {
                write(userId, -excess, CreditLedger.KIND_SETTLE, model, inputTokens, outputTokens,
                        refMessageId, null, null);
            } else if (excess > 0) {
                log.warn("ai.credit.settle_shortfall userId={} reserved={} actual={}", userId, reserved, actual);
                write(userId, 0, CreditLedger.KIND_SETTLE, model, inputTokens, outputTokens, refMessageId, null,
                        "余额不足以补扣差额 " + excess + " 分");
            } else {
                // 实际恰好等于预留：写一条 0 的锚点，让"这次调用被结算过"在流水里看得见
                write(userId, 0, CreditLedger.KIND_SETTLE, model, inputTokens, outputTokens, refMessageId, null, null);
            }
            return;
        }
        long refund = reserved - actual;
        accounts.add(userId, refund);
        write(userId, refund, CreditLedger.KIND_REFUND, model, inputTokens, outputTokens, refMessageId, null, null);
    }

    /** 全额退还：上游一轮都没返回用量、或这次调用根本没发生。 */
    @Transactional
    public void refund(long userId, long reserved, Long refMessageId, String note) {
        if (!billing.isEnabled() || reserved <= 0) {
            return;
        }
        accounts.add(userId, reserved);
        write(userId, reserved, CreditLedger.KIND_REFUND, null, null, null, refMessageId, null, note);
    }

    /**
     * 管理员调整。
     *
     * @return 调整后的余额
     * @throws BizException 扣减时余额不足、或数量为 0
     */
    @Transactional
    public long adjust(long userId, long delta, long operatorUserId, String note) {
        if (delta == 0) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT, "调整数量不能为 0");
        }
        if (delta > 0) {
            accounts.add(userId, delta);
        } else if (accounts.deductIfEnough(userId, -delta) == 0) {
            throw new BizException(ErrorCode.INVALID_ARGUMENT,
                    "该用户余额不足以扣减 " + (-delta) + " 分（当前 " + balance(userId) + " 分）");
        }
        write(userId, delta, CreditLedger.KIND_ADJUST, null, null, null, null, operatorUserId, note);
        return balance(userId);
    }

    /** 本人明细。 */
    public List<LedgerRow> listForUser(long userId, int limit) {
        return ledger.listByUser(userId, clamp(limit)).stream().map(LedgerRow::of).toList();
    }

    /** 管理端审计：全平台最近的流水。 */
    public List<LedgerRow> auditRecent(int limit) {
        return ledger.listRecent(clamp(limit)).stream().map(LedgerRow::of).toList();
    }

    private static int clamp(int limit) {
        return limit <= 0 ? LIST_LIMIT : Math.min(limit, LIST_LIMIT);
    }

    private static int orZero(Integer value) {
        return value == null ? 0 : value;
    }

    private void write(long userId, long delta, String kind, String model, Integer inputTokens,
                       Integer outputTokens, Long refMessageId, Long operatorUserId, String note) {
        CreditLedger row = new CreditLedger();
        row.setUserId(userId);
        row.setDelta(delta);
        row.setKind(kind);
        row.setModel(model);
        row.setInputTokens(inputTokens);
        row.setOutputTokens(outputTokens);
        row.setRefMessageId(refMessageId);
        row.setOperatorUserId(operatorUserId);
        row.setNote(note);
        ledger.insert(row);
    }
}
