package io.github.xiaou61.account.internal.oauth;

import java.time.Instant;

/**
 * 一条第三方登录绑定。
 *
 * <p>对外只暴露 provider / displayName / createdAt：`providerUserId` 是平台侧的标识，
 * 界面上没有用处，泄露出去反而方便别人拼"这个人绑了哪个 GitHub 账号"。
 */
public class UserIdentity {

    private Long id;
    private Long userId;
    private String provider;
    private String providerUserId;
    private String displayName;
    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getProviderUserId() {
        return providerUserId;
    }

    public void setProviderUserId(String providerUserId) {
        this.providerUserId = providerUserId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
