package io.github.xiaou61.ai.internal.conversation;

import java.time.Instant;

/**
 * 一次对话（会话）。
 *
 * <p>可变类 + {@code useGeneratedKeys}，与项目其它实体一致。
 */
class Conversation {

    private Long id;
    private Long userId;
    /** 可选上下文：用户从某个条目页开的对话；不是每个会话都有。 */
    private Long entryId;
    /** 滑动过期时间，由应用按 UTC 写入。 */
    private Instant expiresAt;
    private Instant createdAt;
    private Instant updatedAt;

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

    Long getEntryId() {
        return entryId;
    }

    void setEntryId(Long entryId) {
        this.entryId = entryId;
    }

    Instant getExpiresAt() {
        return expiresAt;
    }

    void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    Instant getUpdatedAt() {
        return updatedAt;
    }

    void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
