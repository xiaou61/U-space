package com.xiaou.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 修改密码请求DTO
 *
 * @author xiaou
 */
@Data
@Schema(description = "修改密码请求")
public class ChangePasswordRequest {

    /**
     * 原密码
     */
    @Schema(description = "原密码", example = "123456")
    @NotBlank(message = "原密码不能为空")
    private String oldPassword;

    /**
     * 新密码
     */
    @Schema(description = "新密码", example = "newPassword123")
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "新密码长度必须在6-20个字符之间")
    private String newPassword;

    /**
     * 确认新密码
     */
    @Schema(description = "确认新密码", example = "newPassword123")
    @NotBlank(message = "确认新密码不能为空")
    private String confirmPassword;
} 