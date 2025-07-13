package com.xiaou.secure.config;

import com.xiaou.secure.properties.SecureProperties;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 安全模块自动配置
 */
@Configuration
@ConditionalOnClass(WebMvcConfigurer.class)
@AutoConfigureAfter(name = "org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration")
public class SecureAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SecureProperties secureProperties() {
        return new SecureProperties();
    }
}