package io.github.xiaou61.note.internal.note;

import java.time.Instant;

/**
 * 一条笔记。
 *
 * <p>可变类 + {@code useGeneratedKeys}，与项目其它实体一致。
 */
class Note {

    private Long id;
    private Long userId;
    /** 可挂靠的知识条目；不是每条笔记都挂。 */
    private Long entryId;
    private String title;
    private String body;
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

    String getTitle() {
        return title;
    }

    void setTitle(String title) {
        this.title = title;
    }

    String getBody() {
        return body;
    }

    void setBody(String body) {
        this.body = body;
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
