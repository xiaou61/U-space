package com.xiaou.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Coze API配置类
 * 管理Coze平台相关的配置信息
 *
 * @author xiaou
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "coze")
public class CozeConfig {

    /**
     * API相关配置
     */
    private Api api = new Api();

    /**
     * 认证相关配置
     */
    private Auth auth = new Auth();

    /**
     * 缓存相关配置
     */
    private Cache cache = new Cache();

    @Data
    public static class Api {
        /**
         * Coze API基础URL
         */
        private String baseUrl = "https://api.coze.cn";

        /**
         * 请求超时时间（毫秒）
         */
        private Integer timeout = 30000;

        /**
         * 重试次数
         */
        private Integer retryCount = 3;

        /**
         * 连接超时时间（毫秒）
         */
        private Integer connectTimeout = 10000;

        /**
         * 读取超时时间（毫秒）
         */
        private Integer readTimeout = 60000;
    }

    @Data
    public static class Auth {
        /**
         * API密钥
         */
        private String apiKey;

        /**
         * 是否启用认证
         */
        private Boolean enabled = true;
    }

    @Data
    public static class Cache {
        /**
         * 是否启用缓存
         */
        private Boolean enabled = true;

        /**
         * 缓存TTL（秒）
         */
        private Long ttl = 3600L;

        /**
         * 缓存Key前缀
         */
        private String keyPrefix = "coze:";
    }
}
