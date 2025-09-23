package com.xiaou.moyu.domain;

import lombok.Data;

import java.util.Map;

/**
 * 热榜分类实体
 *
 * @author xiaou
 */
@Data
public class HotTopicCategory {
    
    /**
     * 分类名称
     */
    private String name;
    
    /**
     * 分类描述
     */
    private String description;
    
    /**
     * API映射 key为平台标识，value为平台描述
     */
    private Map<String, String> apis;
}
