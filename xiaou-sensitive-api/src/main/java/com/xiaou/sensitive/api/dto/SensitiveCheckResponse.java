package com.xiaou.sensitive.api.dto;

import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 敏感词检测响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SensitiveCheckResponse {
    /**
     * 是否命中敏感词
     */
    private Boolean hit;

    /**
     * 命中的敏感词列表
     */
    private List<String> hitWords;

    /**
     * 处理后的文本
     */
    private String processedText;

    /**
     * 风险等级
     */
    private Integer riskLevel;

    /**
     * 执行的动作
     */
    private String action;

    /**
     * 是否允许通过（true-允许发布，false-拒绝发布）
     */
    private Boolean allowed;
} 