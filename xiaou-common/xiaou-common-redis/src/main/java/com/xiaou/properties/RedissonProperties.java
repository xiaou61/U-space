package com.xiaou.properties;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@ConfigurationProperties(prefix = "spring.redis.redisson")
@Data
@Component
public class RedissonProperties {
    private String config;

    private String file;

}