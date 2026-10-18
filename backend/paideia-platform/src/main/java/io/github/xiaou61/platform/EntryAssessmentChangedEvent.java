package io.github.xiaou61.platform;

/**
 * 学习者对某条目改了自评（WORK-010 T-08 的桥）。
 *
 * <p><b>为什么用事件而不是让知识库直接调 AI 模块</b>：自评在 `paideia-knowledge`，记忆在
 * `paideia-ai`——而 `paideia-ai` 已经依赖 `paideia-knowledge`（它要检索条目）。知识库若反过来
 * 依赖 AI，就是循环依赖，构建期就会被 Modulith 拦下。事件把方向拉回来：知识库只宣布
 * "这个人对这条改了什么"，记忆归谁写由 AI 模块自己决定。与 {@link UserDeletedEvent} 同一套做法。
 *
 * <p><b>这一步是"补一个缺口"，不是"改记忆的语义"</b>：`ai_memories_structured` 这一路此前
 * **界面上没有任何写入口**（用户点「记住这条」写的是全文那一路），所以它一直是空的；而助手
 * 只能靠 `MY_MARKS` 工具当场查自评，永远不会"记住"。有了这条事件，自评才真正进入记忆。
 *
 * <p>监听方必须用 {@code @TransactionalEventListener}：自评写入与记忆写入应当是**同一件事**——
 * 自评回滚了却留下一条记忆，是假记忆。
 *
 * @param userId  学习者
 * @param entryId 条目
 * @param level   新等级（`understood` / `unsure`）；{@code null} 表示**取消**标记（该删掉那条记忆）
 */
public record EntryAssessmentChangedEvent(long userId, long entryId, String level) {
}
