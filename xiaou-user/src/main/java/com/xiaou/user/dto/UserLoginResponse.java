package com.xiaou.user.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户登录响应DTO
 *
 * @author xiaou
 */
@Data
public class UserLoginResponse {

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 令牌类型
     */
    private String tokenType = "Bearer";

    /**
     * 过期时间（秒）
     */
    private Long expiresIn;

    /**
     * 用户信息
     */
    private UserInfo userInfo;

    /**
     * 用户信息类
     */
    @Data
    public static class UserInfo {
        /**
         * 用户ID
         */
        private Long id;

        /**
         * 用户名
         */
        private String username;

        /**
         * 昵称
         */
        private String nickname;

        /**
         * 真实姓名
         */
        private String realName;

        /**
         * 邮箱
         */
        private String email;

        /**
         * 手机号
         */
        private String phone;

        /**
         * 头像
         */
        private String avatar;

        /**
         * 性别
         */
        private Integer gender;

        /**
         * 最后登录时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
        private LocalDateTime lastLoginTime;
    }
} 