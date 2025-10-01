package com.xiaou.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置类
 * 
 * @author xiaou
 */
@Configuration
public class CorsConfig {

    /**
     * 跨域过滤器配置
     * 允许所有域名、所有方法、所有请求头的跨域访问
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许所有域名进行跨域调用
        config.addAllowedOriginPattern("*");
        
        // 允许所有请求头
        config.addAllowedHeader("*");
        
        // 允许所有请求方法
        config.addAllowedMethod("*");
        
        // 允许携带凭证（cookies）
        config.setAllowCredentials(true);
        
        // 预检请求的有效期，单位为秒
        config.setMaxAge(3600L);
        
        // 添加映射路径，拦截所有请求
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsFilter(source);
    }
}
