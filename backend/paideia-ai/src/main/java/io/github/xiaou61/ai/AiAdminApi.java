package io.github.xiaou61.ai;

import java.util.List;

/**
 * AI 模块的**管理端**契约（WORK-030）。
 *
 * <p><b>为什么要有这个端口</b>：管理端的"用户列表"要显示每个人的余额、已用积分、调用次数、
 * 最近活跃。这四样分散在 `ai_credit_accounts` / `ai_credit_ledger` / `ai_runs` 三张表里，
 * 而 `paideia-account` 不能依赖本模块（依赖方向反过来也不成立），所以聚合成
 * "按一批 userId 取四个指标"这件事只能由本模块出口。
 *
 * <p><b>调积分也在这里</b>：`CreditService.adjust` 是本模块的内部实现，
 * 管理端要调它只能经一个公共入口。放在同一个端口里，比再开一个只有一个方法的接口清楚。
 */
public interface AiAdminApi {

    /**
     * 一批用户的用量快照。
     *
     * @param userIds 要查的用户 id；空集合直接返回空，不查库
     */
    List<UserUsage> usage(java.util.Collection<Long> userIds);

    /**
     * 管理员调整某人的积分。
     *
     * @return 调整后的余额
     * @throws io.github.xiaou61.platform.BizException 余额不足（扣成负数）时
     */
    long adjust(long userId, long delta, long operatorId, String note);

    /**
     * 一个用户的用量。
     *
     * @param balance      当前余额（没有账户行时 0）
     * @param usedCredits  累计消耗的积分（**只算流出**，即 `delta < 0` 的绝对值之和）
     * @param runCount     累计调用次数
     * @param lastActiveAt 最近一次调用的开始时间（ISO-8601）；从未调用过时为 null
     */
    record UserUsage(long userId, long balance, long usedCredits, long runCount, String lastActiveAt) {
    }
}
