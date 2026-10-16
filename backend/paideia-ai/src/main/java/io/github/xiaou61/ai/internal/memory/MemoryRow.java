package io.github.xiaou61.ai.internal.memory;

import java.time.Instant;

/**
 * 一条记忆行。
 *
 * <p>两路（结构化／全文）共用这一个形状：它们的差别只在**怎么被召回**，不在**存了什么**——
 * 都是一句话加一个时间。为此把结构化那路原来的 `value` 列与全文那路的 `text` 列统一命名为
 * `content`，这样"取值→拼进提示词"只有一条路径，不必在两个类里各写一遍。
 *
 * <p>`kind` 与 `refId` 只有结构化那路有；全文那路读出来是 null。
 * 可变类 + `useGeneratedKeys`，与项目其它实体一致。
 *
 * <p><b>WORK-013 新增的四个字段回答"它为什么在这里"</b>（REQ-010）：`source` 是"谁写的这一条"
 * （用户手动 / 自动整理），`conversationId` 与 `refMessageId` 是自动整理时的出处，`reason` 是模型
 * 给这次判定的理由。
 *
 * <p>{@code source} 跟随**当前内容**：自动整理覆盖了一条手动记忆时它变成 `auto`——那句内容确实
 * 不再是用户写的那句了。面板标签说的是"这句话怎么来的"，不是"这张表一开始谁建的"。
 */
class MemoryRow {

    /** 结构化记忆的种类（写入方给）。 */
    private String kind;
    /** 结构化记忆指向的对象（例如条目 id）。 */
    private Long refId;
    private Long id;
    private Long userId;
    private String content;
    /** 见类注释：这是"当前这条内容是谁写的"（手动 / 自动整理）。 */
    private String source;
    private Long conversationId;
    private Long refMessageId;
    private String reason;
    private Instant createdAt;

    Long getId() {
        return id;
    }

    void setId(Long id) {
        this.id = id;
    }

    Long getUserId() {
        return userId;
    }

    void setUserId(Long userId) {
        this.userId = userId;
    }

    String getKind() {
        return kind;
    }

    void setKind(String kind) {
        this.kind = kind;
    }

    Long getRefId() {
        return refId;
    }

    void setRefId(Long refId) {
        this.refId = refId;
    }

    String getContent() {
        return content;
    }

    void setContent(String content) {
        this.content = content;
    }

    String getSource() {
        return source;
    }

    void setSource(String source) {
        this.source = source;
    }

    Long getConversationId() {
        return conversationId;
    }

    void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    Long getRefMessageId() {
        return refMessageId;
    }

    void setRefMessageId(Long refMessageId) {
        this.refMessageId = refMessageId;
    }

    String getReason() {
        return reason;
    }

    void setReason(String reason) {
        this.reason = reason;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
