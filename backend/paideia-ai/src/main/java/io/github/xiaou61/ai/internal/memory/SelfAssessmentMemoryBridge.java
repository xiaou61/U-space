package io.github.xiaou61.ai.internal.memory;

import io.github.xiaou61.knowledge.EntryContent;
import io.github.xiaou61.knowledge.KnowledgeApi;
import io.github.xiaou61.platform.EntryAssessmentChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * 自评 → 结构化记忆的桥（WORK-010 T-08）。
 *
 * <p><b>它补的是一个真缺口</b>：`ai_memories_structured` 这一路此前**界面上没有任何写入口**
 * （用户点「记住这条」写的是全文那一路），而助手只能靠 `MY_MARKS` 工具当场查自评，永远不会
 * "记住"。有了这条监听，自评才真正进入记忆——这才是"助手知道我哪块弱"该有的样子。
 *
 * <p><b>用的是事件</b>：自评在知识库模块，记忆在 AI 模块，而 AI 已经依赖知识库（要检索条目）；
 * 让知识库反过来调 AI 会成环，构建期就会被 Modulith 拦下。事件把方向拉回单向。
 *
 * <p><b>BEFORE_COMMIT</b>：与自评写入在同一个事务里——自评回滚了却留下一条记忆，是假记忆。
 *
 * <p><b>覆盖语义</b>（AC-008）：同一条目反复改自评，靠唯一键 + `ON DUPLICATE KEY UPDATE`
 * 始终只有一条，内容是最后一次。**这只作用于这条桥**：用户手点「记住这条」写的是全文那一路
 * （另一张表），仍然是追加。
 */
@Component
class SelfAssessmentMemoryBridge {

    private static final Logger log = LoggerFactory.getLogger(SelfAssessmentMemoryBridge.class);

    /**
     * 这条记忆的 kind。
     *
     * <p>它是 {@code ai_memories_structured.kind} 的一个取值，与"自动整理"那套白名单
     * （`weak_topic` / `goal` / `preference`）**不冲突**：那是模型判定的"关于这个人的事实种类"，
     * 这是"这条事实从哪个界面来的"。分开两个值反而能区分"他说自己不懂"与"系统记下他不懂"。
     */
    static final String KIND = "entry_understanding";

    private final MemoryStore memories;
    private final KnowledgeApi knowledge;

    SelfAssessmentMemoryBridge(MemoryStore memories, KnowledgeApi knowledge) {
        this.memories = memories;
        this.knowledge = knowledge;
    }

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    void onChanged(EntryAssessmentChangedEvent event) {
        if (event.level() == null) {
            // 取消了标记：那条记忆必须一起消失，否则标记没了助手还记着，是假记忆
            int removed = memories.forgetEntryAssessment(event.userId(), event.entryId());
            log.info("ai.memory.bridge_cleared userId={} entryId={} removed={}",
                    event.userId(), event.entryId(), removed);
            return;
        }
        // 标题取不到时用 id 兜底：记忆内容要能被人看懂，但**不为拿标题而让自评失败**
        // （条目可能刚被下架，而用户取消标记这条路必须仍然可用——既有设计已经这么定了）
        String title = knowledge.findPublishedContent(event.entryId())
                .map(EntryContent::title)
                .orElse("第 " + event.entryId() + " 条");
        memories.rememberEntryAssessment(event.userId(), event.entryId(), title, event.level());
        log.info("ai.memory.bridge_written userId={} entryId={} level={}",
                event.userId(), event.entryId(), event.level());
    }
}
