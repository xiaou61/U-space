package com.xiaou.sensitive.dto;

import lombok.Data;

/**
 * 高级敏感词检测请求DTO
 *
 * @author xiaou
 */
@Data
public class SensitiveCheckAdvancedRequest {

    /**
     * 要检测的文本
     */
    private String text;

    /**
     * 业务模块
     */
    private String module;

    /**
     * 业务ID
     */
    private Long businessId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 是否检查变形词（默认true）
     */
    private Boolean checkVariant = true;

    /**
     * 是否检查拼音（默认true）
     */
    private Boolean checkPinyin = true;

    /**
     * 是否使用白名单（默认true）
     */
    private Boolean useWhitelist = true;

    /**
     * 是否返回详细信息（默认false）
     */
    private Boolean returnDetails = false;
}
