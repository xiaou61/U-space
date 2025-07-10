package com.xiaou.config;

import com.xiaou.service.SensitiveWordLoader;
import com.xiaou.service.SensitiveWordManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SensitiveAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SensitiveWordManager sensitiveWordManager(SensitiveWordLoader loader) {
        SensitiveWordManager manager = new SensitiveWordManager();
        manager.reload();
        return manager;
    }
}
