package com.xiaou.sensitive.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 敏感词实体类
 */
@Data
public class SensitiveWord {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 敏感词
     */
    private String word;

    /**
     * 词类型 1-普通词 2-正则表达式
     */
    private Integer wordType;

    /**
     * 拼音（用于拼音检测）
     */
    private String pinyin;

    /**
     * 分类ID
     */
    private Integer categoryId;

    /**
     * 风险等级 1-低 2-中 3-高
     */
    private Integer level;

    /**
     * 处理动作 1-替换 2-拒绝 3-审核
     */
    private Integer action;

    /**
     * 启用变形词检测 0-否 1-是
     */
    private Integer enableVariantCheck;

    /**
     * 状态 0-禁用 1-启用
     */
    private Integer status;

    /**
     * 创建人ID
     */
    private Long creatorId;

    /**
     * 备注
     */
    private String remark;

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