package com.xiaou.sensitive.api.dto;

import lombok.Data;

/**
 * 敏感词检测请求DTO
 */
@Data
public class SensitiveCheckRequest {
    /**
     * 要检测的文本内容
     */
    private String text;

    /**
     * 模块名称（如：community、interview、moment）
     */
    private String module;

    /**
     * 业务ID（可选）
     */
    private Long businessId;

    /**
     * 用户ID（可选）
     */
    private Long userId;
} 