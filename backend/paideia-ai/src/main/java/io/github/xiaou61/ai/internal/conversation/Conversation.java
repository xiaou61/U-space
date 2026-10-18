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
    /**
     * 自动整理**已经整理到哪条消息为止**（WORK-013）。
     *
     * <p>它是增量的游标，也是并发单飞的判据：`UPDATE ... WHERE 游标还是我读到的那一个` 抢到的线程
     * 才会去跑整理。两件事共用一个字段是刻意的——分成"锁"和"游标"两个字段，它们迟早会不一致。
     */
    private Long lastMemoryExtractMessageId;
    /**
     * 这段会话用哪套公共行为（WORK-010）。`null` = 通用助手（既有会话全是 NULL，行为不变）。
     *
     * <p>恢复会话时按它选行为，所以**从通用入口继续一段面试对话仍是面试行为**——行为跟着
     * 会话走，比跟着入口走更符合"那是同一个人说过的话"。
     */
    private String harness;
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

    Long getLastMemoryExtractMessageId() {
        return lastMemoryExtractMessageId;
    }

    void setLastMemoryExtractMessageId(Long lastMemoryExtractMessageId) {
        this.lastMemoryExtractMessageId = lastMemoryExtractMessageId;
    }

    String getHarness() {
        return harness;
    }

    void setHarness(String harness) {
        this.harness = harness;
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
