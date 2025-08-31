package com.xiaou.system.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码编码器配置类
 * 独立配置以避免循环依赖
 *
 * @author xiaou
 */
@Configuration
public class PasswordConfig {

    /**
     * 密码加密器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new HutoolPasswordEncoder();
    }
} 