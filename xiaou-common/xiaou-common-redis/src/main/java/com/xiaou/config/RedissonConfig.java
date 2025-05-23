package com.xiaou.config;

import com.xiaou.properties.RedissonProperties;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@Slf4j
public class RedissonConfig {

    @Autowired
    private RedissonProperties redissonProperties;

    @Bean
    public RedissonClient redissonClient() throws Exception{
        Config config = Config.fromYAML(redissonProperties.getConfig());
        Redisson.create(config);
        log.info("Redisson 已启动");
        return Redisson.create(config);
    }
}