package com.xiaou.sse.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * SSE 配置项
 */
@Data
@ConfigurationProperties("sse")
public class SseProperties {

    private Boolean enabled;

    /**
     * 路径
     */
    private String path;
}
