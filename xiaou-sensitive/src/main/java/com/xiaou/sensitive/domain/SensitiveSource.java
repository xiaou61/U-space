package com.xiaou.sensitive.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 敏感词来源管理实体
 *
 * @author xiaou
 */
@Data
public class SensitiveSource {

    /**
     * 主键ID
     */
    private Integer id;

    /**
     * 词库来源名称
     */
    private String sourceName;

    /**
     * 来源类型（local/api/github）
     */
    private String sourceType;

    /**
     * API地址
     */
    private String apiUrl;

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * 同步间隔（小时）
     */
    private Integer syncInterval;

    /**
     * 最后同步时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastSyncTime;

    /**
     * 同步状态 0-失败 1-成功
     */
    private Integer syncStatus;

    /**
     * 词汇数量
     */
    private Integer wordCount;

    /**
     * 状态 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
