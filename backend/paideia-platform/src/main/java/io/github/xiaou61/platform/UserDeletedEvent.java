package io.github.xiaou61.platform;

/**
 * 账号被注销。
 *
 * <p><b>为什么用应用内事件而不是让账号模块直接调各模块</b>：注销要删的是散在多个模块里的
 * 用户数据（AI 的会话与记忆、笔记与分享…）。若让 `paideia-account` 去调它们的 API，账号模块会
 * 反向依赖所有业务模块；而账号是地基，这个方向不对。事件把"谁删了什么"留在各模块自己手里：
 * 账号只负责删自己那两行并宣布"这个人没了"。
 *
 * <p>放在 {@code paideia-platform} 而不是账号模块的包根：那样每个监听方都要多依赖一个模块。
 * 平台模块是所有模块都已经依赖的公共契约，事件放这里最省事。
 *
 * <p>监听方必须用 {@code @TransactionalEventListener(BEFORE_COMMIT)}：注销是"要么全删干净、
 * 要么什么都没发生"的操作，分两个事务做会出现"用户没了但记忆还在"的中间状态。
 */
public record UserDeletedEvent(long userId) {
}
