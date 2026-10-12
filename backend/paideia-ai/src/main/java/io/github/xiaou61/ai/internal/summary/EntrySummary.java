package io.github.xiaou61.ai.internal.summary;

import java.time.Instant;

/**
 * 精简结果（每条目每人一行）。
 *
 * <p>可变类 + {@code useGeneratedKeys}，与项目其它实体一致。
 */
class EntrySummary {

    private Long id;
    private Long entryId;
    private String contentHash;
    private String model;
    private String promptVersion;
    private Long ownerUserId;
    /** public | private */
    private String visibility;
    /** 已通过字段校验的卡片 JSON。 */
    private String cardJson;
    private Instant createdAt;

    Long getId() {
        return id;
    }

    void setId(Long id) {
        this.id = id;
    }

    Long getEntryId() {
        return entryId;
    }

    void setEntryId(Long entryId) {
        this.entryId = entryId;
    }

    String getContentHash() {
        return contentHash;
    }

    void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }

    String getModel() {
        return model;
    }

    void setModel(String model) {
        this.model = model;
    }

    String getPromptVersion() {
        return promptVersion;
    }

    void setPromptVersion(String promptVersion) {
        this.promptVersion = promptVersion;
    }

    Long getOwnerUserId() {
        return ownerUserId;
    }

    void setOwnerUserId(Long ownerUserId) {
        this.ownerUserId = ownerUserId;
    }

    String getVisibility() {
        return visibility;
    }

    void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    String getCardJson() {
        return cardJson;
    }

    void setCardJson(String cardJson) {
        this.cardJson = cardJson;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
