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
 * 使用 Sa-Token 独立 Redis 插件（sa-token-alone-redis + Jedis）
 * 配置在 application.yml 中的 sa-token.alone-redis 节点
 * 这样可以让 Sa-Token 使用独立的 Redis 连接池，不影响现有的 Redisson 配置
 * 
 * 重要说明：
 * 1. 需要同时引入 sa-token-alone-redis 和 jedis 依赖
 * 2. 项目重启后，只要 Redis 服务不重启，Token 数据不会丢失
 * 3. SaToken 会自动将 Token 数据持久化到 Redis
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
            // 管理端路由认证（路径是 /auth/**）
            SaRouter.match("/auth/**")
                .notMatch("/auth/login", "/auth/register", "/auth/refresh")
                .check(r -> StpAdminUtil.checkLogin());
            
            // 用户端路由认证（路径是 /user/**）
            SaRouter.match("/user/**")
                .notMatch("/user/auth/login", "/user/auth/register", "/user/auth/refresh")
                .notMatch("/user/auth/check-username", "/user/auth/check-email")
                .check(r -> StpUserUtil.checkLogin());
            
            // 验证码接口无需认证
            SaRouter.match("/captcha/**").stop();
            
            // Swagger 和 API 文档接口无需认证
            SaRouter.match("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").stop();
            
        })).addPathPatterns("/**")
          .excludePathPatterns(
              "/error",
              "/favicon.ico"
          );
        
        log.info("✅ Sa-Token 拦截器注册成功");
        log.info("✅ Sa-Token 使用独立 Redis 连接池（sa-token-alone-redis + Jedis），不影响业务 Redisson 配置");
    }
}
