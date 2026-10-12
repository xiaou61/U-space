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
 */
class MemoryRow {

    /** 结构化记忆的种类（写入方给）。 */
    private String kind;
    /** 结构化记忆指向的对象（例如条目 id）。 */
    private Long refId;
    private Long id;
    private Long userId;
    private String content;
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

    Instant getCreatedAt() {
        return createdAt;
    }

    void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
