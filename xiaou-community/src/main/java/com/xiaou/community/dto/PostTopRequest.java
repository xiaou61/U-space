package com.xiaou.community.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 帖子置顶请求
 * 
 * @author xiaou
 */
@Data
public class PostTopRequest {
    
    /**
     * 置顶时长（小时）
     */
    @NotNull(message = "置顶时长不能为空")
    private Integer duration;
} 