package io.github.xiaou61.ai.internal.memory;

import java.util.Set;

/**
 * 允许写进**结构化那一路**的 `kind` 取值（WORK-013）。
 *
 * <p><b>为什么写死枚举而不是让模型自由发挥</b>：`kind` 是召回与展示时"这是什么类型的事实"的标签，
 * 自由文本会让它退化成一句废话（每个模型都发明自己的词），也就没法按类型筛。取值越界不是错误——
 * 那条候选**落到全文那路**即可（"他说过的话"本来就不需要分类），所以这里是"规范化"而不是"拒绝"。
 *
 * <p>WORK-006 定下 `kind` 由写入方给、是字符串（改枚举要迁移）；本条是**自动整理这个写入方**
 * 自己收的口径——用户手动写入仍不受限。
 */
final class MemoryKinds {

    /** 还不懂 / 卡住的知识点。 */
    static final String WEAK_TOPIC = "weak_topic";
    /** 学习目标（"准备秋招"）。 */
    static final String GOAL = "goal";
    /** 偏好（"喜欢先看例子"）。 */
    static final String PREFERENCE = "preference";

    private static final Set<String> ALLOWED = Set.of(WEAK_TOPIC, GOAL, PREFERENCE);

    private MemoryKinds() {
    }

    /**
     * 把模型给的 kind 规范成白名单里的一个。
     *
     * @return 白名单内的规范值；不认识或没给则返回 {@code null}（调用方据此改走全文那路）
     */
    static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim().toLowerCase(java.util.Locale.ROOT);
        return ALLOWED.contains(trimmed) ? trimmed : null;
    }
}
