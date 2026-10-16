package io.github.xiaou61.ai.internal.settings;

import java.time.Instant;

/**
 * 一个用户的 AI 设置（WORK-013）。
 *
 * <p><b>三个开关是"两条轴 + 一个总闸"</b>，照 ChatGPT 的控制面：
 * <ul>
 *   <li>{@code memoryEnabled}：总闸。关掉 = 不召回、不自动整理、手动写入也被拒。</li>
 *   <li>{@code referenceSavedMemories}：用不用"已保存的记忆"（只管读）。</li>
 *   <li>{@code referenceChatHistory}：用不用"当前对话的历史消息"（关掉 = 只回答本轮这一句）。</li>
 * </ul>
 *
 * <p>**一人一行，不按身份切**——WORK-010 已裁"记忆全属于你一份"，本表同理。
 *
 * <p>可变类 + 取值类型，与项目其它实体一致。
 */
class AiUserSettings {

    private Long userId;
    private boolean memoryEnabled;
    private boolean referenceSavedMemories;
    private boolean referenceChatHistory;
    private Instant updatedAt;

    Long getUserId() {
        return userId;
    }

    void setUserId(Long userId) {
        this.userId = userId;
    }

    boolean isMemoryEnabled() {
        return memoryEnabled;
    }

    void setMemoryEnabled(boolean memoryEnabled) {
        this.memoryEnabled = memoryEnabled;
    }

    boolean isReferenceSavedMemories() {
        return referenceSavedMemories;
    }

    void setReferenceSavedMemories(boolean referenceSavedMemories) {
        this.referenceSavedMemories = referenceSavedMemories;
    }

    boolean isReferenceChatHistory() {
        return referenceChatHistory;
    }

    void setReferenceChatHistory(boolean referenceChatHistory) {
        this.referenceChatHistory = referenceChatHistory;
    }

    Instant getUpdatedAt() {
        return updatedAt;
    }

    void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
