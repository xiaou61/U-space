package io.github.xiaou61.ai;

/**
 * 自动整理对一条记忆的判定结果（WORK-013）。
 *
 * <p>它的字段就是**模型允许对我们的库做的四件事**：`ADD` 新增、`UPDATE` 覆盖、`DELETE` 撤掉、
 * `SKIP` 什么都不做。四者都在提示词里写死语义，再由 {@code MemoryCandidateParser} 逐字段校验——
 * 模型的输出是不可信输入，缺字段、编造 id、超长一律整次放弃。
 *
 * @param action  四种动作之一
 * @param store   写到/指向哪一路（`structured` = 关于学习者的事实，`text` = 他说过的话）。
 *                `UPDATE`/`DELETE` 时它指的是**那条已有记忆在哪一路**——两路的 id 各自独立，
 *                只说 id 会改错表。
 * @param targetId 仅 `UPDATE`/`DELETE` 用：要改/删的那条记忆 id
 * @param kind     仅写入结构化那路时用，取值受限（见 {@code MemoryKinds}）；越界会被规范成全文那路
 * @param refId    可选：这条事实指向的对象（例如条目 id）
 * @param content  要写入的内容（`ADD`/`UPDATE` 必给）；`DELETE`/`SKIP` 时忽略
 * @param reason   模型给这次判定的理由，会随记忆入库，面板能显示
 */
public record MemoryCandidate(Action action, Store store, Long targetId, String kind, Long refId,
                              String content, String reason) {

    public enum Action {
        ADD,
        UPDATE,
        DELETE,
        SKIP
    }

    public enum Store {
        /** 关于学习者的既成事实。 */
        STRUCTURED,
        /** 他说过的话 / 偏好。 */
        TEXT
    }
}
