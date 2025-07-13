package com.xiaou.secure.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 安全模块配置
 */
@ConfigurationProperties(prefix = "secure")
public class SecureProperties {

    /**
     * AES 密钥（16/24/32 位）
     */
    // 默认 16 字符，避免 InvalidKeyException
    private String aesKey = "xiaou-secure-123";

    /**
     * 签名密钥
     */
    private String signSecret = "xiaou-sign-secret";

    /**
     * 允许的时间差 (秒)，默认 300 秒
     */
    private long allowedTimestampOffset = 300;

    public String getAesKey() {
        return aesKey;
    }

    public void setAesKey(String aesKey) {
        this.aesKey = aesKey;
    }

    public String getSignSecret() {
        return signSecret;
    }

    public void setSignSecret(String signSecret) {
        this.signSecret = signSecret;
    }

    public long getAllowedTimestampOffset() {
        return allowedTimestampOffset;
    }

    public void setAllowedTimestampOffset(long allowedTimestampOffset) {
        this.allowedTimestampOffset = allowedTimestampOffset;
    }
}