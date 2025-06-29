package com.xiaou.redis.config;


import com.xiaou.redis.properties.RedissonProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@Slf4j
public class RedissonConfig {

    @Resource
    private RedissonProperties redissonProperties;

    @Bean
    public RedissonClient redissonClient() throws Exception{
        Config config = Config.fromYAML(redissonProperties.getConfig());
        Redisson.create(config);
        log.info("Redisson 已启动");
        return Redisson.create(config);
    }
}