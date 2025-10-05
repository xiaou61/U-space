package com.xiaou.sensitive.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 敏感词白名单实体
 *
 * @author xiaou
 */
@Data
public class SensitiveWhitelist {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 白名单词汇
     */
    private String word;

    /**
     * 分类（专业术语/成语/人名/品牌等）
     */
    private String category;

    /**
     * 加入白名单的原因
     */
    private String reason;

    /**
     * 作用范围（global-全局/module-模块级）
     */
    private String scope;

    /**
     * 模块名称（scope=module时有效）
     */
    private String moduleName;

    /**
     * 状态 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 创建人ID
     */
    private Long creatorId;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
