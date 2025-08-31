package com.xiaou.user.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 用户登录请求DTO
 *
 * @author xiaou
 */
@Data
public class UserLoginRequest {

    /**
     * 用户名或邮箱
     */
    @NotBlank(message = "用户名不能为空")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    /**
     * 验证码
     */
    @NotBlank(message = "验证码不能为空")
    private String captcha;

    /**
     * 验证码标识
     */
    @NotBlank(message = "验证码标识不能为空")
    private String captchaKey;

    /**
     * 记住我
     */
    private Boolean rememberMe = false;
} 