package com.xiaou.community.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 用户封禁请求
 * 
 * @author xiaou
 */
@Data
public class UserBanRequest {
    
    /**
     * 封禁原因
     */
    @NotBlank(message = "封禁原因不能为空")
    private String reason;
    
    /**
     * 封禁时长（小时）
     */
    @NotNull(message = "封禁时长不能为空")
    private Integer duration;
} 