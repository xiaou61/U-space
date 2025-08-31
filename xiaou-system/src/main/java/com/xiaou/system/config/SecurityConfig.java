package com.xiaou.system.config;

import com.xiaou.system.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * 管理员端 Spring Security 配置类
 * 
 * 禁用默认认证，使用自定义JWT认证
 * 只处理管理员相关路径
 *
 * @author xiaou
 */
@Configuration
@RequiredArgsConstructor
@Order(2) // 优先级低于用户配置
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 管理员端安全过滤器链配置
     * 只处理管理员相关路径
     */
    @Bean
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        http
            // 只处理管理员相关路径，排除用户路径
            .securityMatcher(request -> {
                String path = request.getRequestURI();
                // 不处理用户路径和验证码路径
                return !path.startsWith("/user/") && !path.startsWith("/captcha/");
            })
            
            // 禁用CSRF（因为使用JWT）
            .csrf(csrf -> csrf.disable())
            
            // 配置CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 配置会话管理 - 完全无状态
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 禁用所有默认认证机制
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(formLogin -> formLogin.disable())
            .logout(logout -> logout.disable())
            
            // 配置请求授权
            .authorizeHttpRequests(authz -> authz
                // 管理员登录接口无需认证
                .requestMatchers("/auth/login").permitAll()
                // 允许Swagger相关接口
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                // 允许OpenAPI文档
                .requestMatchers("/v3/api-docs", "/v3/api-docs/swagger-config").permitAll()
                // 允许Druid监控接口
                .requestMatchers("/druid/**").permitAll()
                // 允许静态资源
                .requestMatchers("/favicon.ico", "/error").permitAll()
                // 允许OPTIONS请求（CORS预检）
                .requestMatchers("OPTIONS", "/**").permitAll()
                // 管理员相关接口需要认证
                .requestMatchers("/auth/**", "/admin/**", "/log/**").authenticated()
                // 其他接口根据具体情况处理
                .anyRequest().authenticated()
            )
            
            // 添加管理员JWT过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS配置
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 允许的源
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        
        // 允许的HTTP方法
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // 允许的请求头
        configuration.setAllowedHeaders(Arrays.asList("*"));
        
        // 允许发送Cookie
        configuration.setAllowCredentials(true);
        
        // 预检请求的缓存时间
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
} 