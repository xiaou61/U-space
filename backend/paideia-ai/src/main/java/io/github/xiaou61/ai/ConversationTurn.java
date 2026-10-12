package io.github.xiaou61.ai;

/**
 * 对话里的一条历史消息（端口层）。
 *
 * <p>只有两种角色，与库里 {@code ai_messages.role} 的取值一一对应。
 * 用枚举而不是字符串：这层是跨模块契约，拼错一个字符串要到运行时才发现。
 */
public record ConversationTurn(Role role, String content) {

    public enum Role {
        USER,
        ASSISTANT;

        /** 库里存的是小写。 */
        public String wireName() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }

        public static Role of(String wireName) {
            return ASSISTANT.wireName().equals(wireName) ? ASSISTANT : USER;
        }
    }
}
