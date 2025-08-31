package com.xiaou.system.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3.0 配置类
 *
 * @author xiaou
 */
@Configuration
public class OpenApiConfig {

    /**
     * OpenAPI 3.0 配置
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .addSecurityItem(new SecurityRequirement().addList("Bearer Token"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Token",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("请输入JWT Token，格式：Bearer {token}")
                        )
                );
    }

    /**
     * API 信息
     */
    private Info apiInfo() {
        return new Info()
                .title("Code-Nest 管理后台 API")
                .description("Code-Nest 项目的后台管理系统 API 文档")
                .version("1.0.0")
                .contact(new Contact()
                        .name("xiaou")
                        .email("xiaou@code-nest.com")
                        .url("https://github.com/xiaou/code-nest")
                )
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT")
                );
    }
} 