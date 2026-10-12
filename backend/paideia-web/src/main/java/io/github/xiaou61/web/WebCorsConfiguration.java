package io.github.xiaou61.web;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 跨域放行列表来自配置，只作用于 {@code /api/**}；顺带把管理端的目录入口接上。
 *
 * <p>桌面壳的页面来源不是 {@code http(s)} 域（Tauri 使用自定义协议），必须在这里显式加入
 * 允许列表，否则桌面端调不通后端；只配置 Web 端会让"Web 能用、exe 报跨域"。
 * 未开启 {@code allowCredentials}：令牌通过 Authorization 头传递，不依赖 Cookie。
 */
@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class WebCorsConfiguration implements WebMvcConfigurer {

    private final CorsProperties properties;

    public WebCorsConfiguration(CorsProperties properties) {
        this.properties = properties;
    }

    /**
     * 管理端的入口是 {@code /admin/} 这个目录地址，而不是 {@code /admin/index.html}。
     *
     * <p>为什么需要转发而不是靠静态资源：静态资源处理器不会把目录请求映射到目录下的
     * index.html（实测 {@code GET /admin/} 是 404）。两个前端各占一个地址——学习者端在
     * {@code /}、管理端在 {@code /admin/}——但它们由**同一个进程**提供，因为这台机器对公网
     * 只开了一个端口（8080/80/81 都被安全组挡着，实测超时）；要真拆成两个端口，先得有第二个
     * 可达端口。
     *
     * <p>只处理目录入口：SPA 用 Hash 路由，深链接（{@code /admin/#/knowledge/entries}）本来
     * 就只会请求 {@code /admin/}，不必为每条前端路由再登记一次。
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/admin").setViewName("forward:/admin/index.html");
        registry.addViewController("/admin/").setViewName("forward:/admin/index.html");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (properties.allowedOrigins().isEmpty()) {
            return;
        }
        String[] origins = properties.allowedOrigins().toArray(String[]::new);

        registry.addMapping("/api/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);

        // 注意：Actuator 的端点由它自己的 HandlerMapping 处理，这里的注册对它们无效。
        // 健康检查的跨域必须用 Boot 的 management.endpoints.web.cors.* 配置，
        // 两处都要配，否则前端能调 /api 却调不通 /actuator/health。
    }
}
