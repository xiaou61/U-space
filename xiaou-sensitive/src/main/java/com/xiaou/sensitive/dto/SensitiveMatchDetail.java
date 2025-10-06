package com.xiaou.sensitive.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 敏感词匹配详情
 *
 * @author xiaou
 */
@Data
@Builder
public class SensitiveMatchDetail {

    /**
     * 匹配到的敏感词
     */
    private String word;

    /**
     * 在文本中的位置
     */
    private Integer position;

    /**
     * 分类
     */
    private String category;

    /**
     * 风险等级
     */
    private Integer level;

    /**
     * 匹配类型（exact-精确/variant-变形/pinyin-拼音）
     */
    private String matchType;
}
