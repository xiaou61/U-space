package io.github.xiaou61.ai.internal.conversation;

import java.time.Instant;

/**
 * 会话里的一条消息。
 *
 * <p>只有两种角色：{@code user} 与 {@code assistant}。**思考过程不落库**——它体积大、对后续轮次
 * 没有用途，只在当次响应的折叠区里展示（见 design 的「实现选择」）。
 */
class ConversationMessage {

    /** 角色常量。用常量而不是枚举：值直接进 VARCHAR 列，枚举与数据库之间的映射没有额外价值。 */
    static final String ROLE_USER = "user";
    static final String ROLE_ASSISTANT = "assistant";

    private Long id;
    private Long conversationId;
    private String role;
    private String content;
    /** 只有助手消息有；BYOK 下用量要返还用户，所以随消息存一份 JSON。 */
    private String tokenUsageJson;
    private Instant createdAt;

    Long getId() {
        return id;
    }

    void setId(Long id) {
        this.id = id;
    }

    Long getConversationId() {
        return conversationId;
    }

    void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    String getRole() {
        return role;
    }

    void setRole(String role) {
        this.role = role;
    }

    String getContent() {
        return content;
    }

    void setContent(String content) {
        this.content = content;
    }

    String getTokenUsageJson() {
        return tokenUsageJson;
    }

    void setTokenUsageJson(String tokenUsageJson) {
        this.tokenUsageJson = tokenUsageJson;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
