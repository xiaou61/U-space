package com.xiaou.sensitive.dto;

import lombok.Data;

/**
 * 热门敏感词VO
 *
 * @author xiaou
 */
@Data
public class HotWordVO {

    /**
     * 敏感词
     */
    private String word;

    /**
     * 命中次数
     */
    private Integer hitCount;

    /**
     * 分类
     */
    private String category;

    /**
     * 业务模块
     */
    private String module;
}
