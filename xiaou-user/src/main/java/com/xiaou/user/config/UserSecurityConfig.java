package com.xiaou.user.config;

import com.xiaou.user.security.UserJwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 用户端 Spring Security 配置
 * 
 * @author xiaou
 */
@Configuration
@RequiredArgsConstructor
@Order(1) // 优先级高于管理员配置
public class UserSecurityConfig {

    private final UserJwtAuthenticationFilter userJwtAuthenticationFilter;

    /**
     * 用户端安全过滤器链配置
     * 只处理 /api/user/** 和 /api/captcha/** 路径
     */
    @Bean
    public SecurityFilterChain userFilterChain(HttpSecurity http) throws Exception {
        http
            // 只处理用户相关路径
            .securityMatcher("/user/**", "/captcha/**")
            
            // 禁用CSRF（因为使用JWT）
            .csrf(csrf -> csrf.disable())
            
            // 配置会话管理 - 完全无状态
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 禁用所有默认认证机制
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(formLogin -> formLogin.disable())
            .logout(logout -> logout.disable())
            
            // 配置请求授权
            .authorizeHttpRequests(authz -> authz
                // 用户认证相关接口无需认证
                .requestMatchers("/user/auth/login", "/user/auth/register").permitAll()
                .requestMatchers("/user/auth/check-username", "/user/auth/check-email").permitAll()
                // 验证码接口无需认证
                .requestMatchers("/captcha/**").permitAll()
                // 其他用户接口需要认证
                .requestMatchers("/user/**").authenticated()
            )
            
            // 添加用户JWT过滤器
            .addFilterBefore(userJwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
} 