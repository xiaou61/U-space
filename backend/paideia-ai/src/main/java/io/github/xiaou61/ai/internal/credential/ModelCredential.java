package io.github.xiaou61.ai.internal.credential;

import java.time.Instant;

/**
 * 用户模型凭据（一行一个用户）。
 *
 * <p>可变类而不是 record：本项目统一用 {@code useGeneratedKeys} 回写自增主键（与知识条目一致）。
 *
 * <p><b>{@link #toString()} 被刻意覆写</b>：默认实现会把密文（有时还有解密后的明文）打出来，
 * 而日志里出现 Key 或密文按缺陷处理。这里只给自己想看的东西。
 */
class ModelCredential {

    private Long id;
    private Long userId;
    private String baseUrl;
    private String model;
    /** 密文：iv(12) || ciphertext || tag(16)。 */
    private byte[] apiKeyCipher;
    private String apiKeyLast4;
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

    String getBaseUrl() {
        return baseUrl;
    }

    void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    String getModel() {
        return model;
    }

    void setModel(String model) {
        this.model = model;
    }

    byte[] getApiKeyCipher() {
        return apiKeyCipher;
    }

    void setApiKeyCipher(byte[] apiKeyCipher) {
        this.apiKeyCipher = apiKeyCipher;
    }

    String getApiKeyLast4() {
        return apiKeyLast4;
    }

    void setApiKeyLast4(String apiKeyLast4) {
        this.apiKeyLast4 = apiKeyLast4;
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

    @Override
    public String toString() {
        return "ModelCredential[userId=" + userId + ", baseUrl=" + baseUrl + ", model=" + model
                + ", apiKey=***, last4=" + apiKeyLast4 + "]";
    }
}
