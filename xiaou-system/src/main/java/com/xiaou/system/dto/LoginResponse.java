package com.xiaou.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录响应DTO
 *
 * @author xiaou
 */
@Data
@Accessors(chain = true)
@Schema(description = "登录响应结果")
public class LoginResponse {

    /**
     * 访问令牌
     */
    @Schema(description = "访问令牌", example = "eyJhbGciOiJIUzUxMiJ9...")
    private String accessToken;

    /**
     * 刷新令牌
     */
    @Schema(description = "刷新令牌", example = "eyJhbGciOiJIUzUxMiJ9...")
    private String refreshToken;

    /**
     * 令牌类型
     */
    @Schema(description = "令牌类型", example = "Bearer")
    private String tokenType = "Bearer";

    /**
     * 令牌过期时间（秒）
     */
    @Schema(description = "令牌过期时间（秒）", example = "7200")
    private Long expiresIn;

    /**
     * 用户信息
     */
    @Schema(description = "用户信息")
    private UserInfo userInfo;

    /**
     * 用户信息内部类
     */
    @Data
    @Accessors(chain = true)
    @Schema(description = "用户信息")
    public static class UserInfo {
        /**
         * 用户ID
         */
        @Schema(description = "用户ID", example = "1")
        private Long id;

        /**
         * 用户名
         */
        @Schema(description = "用户名", example = "admin")
        private String username;

        /**
         * 真实姓名
         */
        @Schema(description = "真实姓名", example = "超级管理员")
        private String realName;

        /**
         * 邮箱
         */
        @Schema(description = "邮箱", example = "admin@code-nest.com")
        private String email;

        /**
         * 头像
         */
        @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
        private String avatar;

        /**
         * 最后登录时间
         */
        @Schema(description = "最后登录时间", example = "2024-01-01 10:00:00")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private LocalDateTime lastLoginTime;

        /**
         * 角色列表
         */
        @Schema(description = "角色列表", example = "[\"SUPER_ADMIN\"]")
        private List<String> roles;

        /**
         * 权限列表
         */
        @Schema(description = "权限列表", example = "[\"system:admin:query\", \"system:admin:add\"]")
        private List<String> permissions;
    }
} 