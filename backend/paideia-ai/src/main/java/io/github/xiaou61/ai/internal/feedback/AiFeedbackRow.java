package io.github.xiaou61.ai.internal.feedback;

import java.time.Instant;

/**
 * 一行反馈。可变类而不是 record：MyBatis 的 `useGeneratedKeys` 要往 `id` 里回写。
 */
class AiFeedbackRow {

    private Long id;
    private Long userId;
    private Long conversationId;
    private Long messageId;
    private String traceId;
    private String kind;
    private String rating;
    private String reason;
    private Instant createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public String getKind() { return kind; }
    public void setKind(String kind) { this.kind = kind; }
    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
