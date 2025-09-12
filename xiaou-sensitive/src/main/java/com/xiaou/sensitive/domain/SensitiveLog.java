package com.xiaou.sensitive.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 敏感词检测日志实体类
 */
@Data
public class SensitiveLog {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 模块名称
     */
    private String module;

    /**
     * 业务ID
     */
    private Long businessId;

    /**
     * 原始文本
     */
    private String originalText;

    /**
     * 命中的敏感词（JSON格式）
     */
    private String hitWords;

    /**
     * 执行的动作
     */
    private String action;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
} 