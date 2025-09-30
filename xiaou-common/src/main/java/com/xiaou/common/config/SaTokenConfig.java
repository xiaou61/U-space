package com.xiaou.common.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import com.xiaou.common.satoken.StpAdminUtil;
import com.xiaou.common.satoken.StpUserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置类
 * 
 * 使用 Sa-Token 独立 Redis 插件（sa-token-alone-redis）
 * 配置在 application.yml 中的 sa-token.alone-redis 节点
 * 这样可以让 Sa-Token 使用独立的 Redis 连接池，不影响现有的 Redisson 配置
 * 
 * @author xiaou
 */
@Slf4j
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {
    
    /**
     * 注册 Sa-Token 拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 Sa-Token 拦截器
        registry.addInterceptor(new SaInterceptor(handler -> {
            // 管理端路由认证
            SaRouter.match("/admin/**")
                .notMatch("/admin/auth/login", "/admin/auth/register", "/admin/auth/refresh")  // 排除登录注册
                .check(r -> StpAdminUtil.checkLogin());  // 检查管理员登录
            
            // 用户端路由认证
            SaRouter.match("/user/**")
                .notMatch("/user/auth/login", "/user/auth/register", "/user/auth/refresh")  // 排除登录注册
                .notMatch("/user/auth/check-username", "/user/auth/check-email")  // 排除用户名和邮箱检查
                .check(r -> StpUserUtil.checkLogin());  // 检查用户登录
            
            // 验证码接口无需认证
            SaRouter.match("/captcha/**").stop();
            
            // Swagger 和 API 文档接口无需认证
            SaRouter.match("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").stop();
            
        })).addPathPatterns("/**")  // 拦截所有路由
          .excludePathPatterns(
              "/error",
              "/favicon.ico"
          );
        
        log.info("✅ Sa-Token 拦截器注册成功");
        log.info("✅ Sa-Token 使用独立 Redis 连接池（database: 4），不影响业务 Redisson 配置");
    }
}
