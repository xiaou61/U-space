package io.github.xiaou61.ai.internal.conversation;

import java.util.List;

/**
 * 会话窗口的裁剪规则。**纯函数**：不碰数据库、不碰时间，因此可以快速单测。
 *
 * <p>两件事分开：进模型上下文的条数（{@code maxContextMessages}）是"看得见多少历史"，
 * 落库保留的条数（{@code maxStoredMessages}）是"库里留多少"。前者小、后者大——
 * 截断上下文不丢数据，超出存储上限才真删最旧的。
 */
final class ConversationWindow {

    private ConversationWindow() {
    }

    /**
     * 取最近 {@code maxMessages} 条。
     *
     * @param oldestFirst 输入必须是**旧 → 新**的顺序（直接来自数据库的倒序查询翻转后）
     * @return 仍然是旧 → 新；不改动入参
     */
    static <T> List<T> lastMessages(List<T> oldestFirst, int maxMessages) {
        if (maxMessages <= 0 || oldestFirst.isEmpty()) {
            return List.of();
        }
        if (oldestFirst.size() <= maxMessages) {
            return List.copyOf(oldestFirst);
        }
        return List.copyOf(oldestFirst.subList(oldestFirst.size() - maxMessages, oldestFirst.size()));
    }

    /**
     * 写入后超出存储上限时，要删掉的最旧条数。
     *
     * <p>{@code maxStored < 1} 视为"没有上限"、返回 0：配置写错时**宁可什么都不删**。
     * 反过来解释（0 = 全删）会让一个手误清空用户全部历史，代价不对等。
     *
     * @param storedCount 本次写入**之后**该会话的总条数
     */
    static int messagesToDrop(long storedCount, int maxStored) {
        if (maxStored < 1) {
            return 0;
        }
        long drop = storedCount - maxStored;
        return drop <= 0 ? 0 : (int) drop;
    }
}
