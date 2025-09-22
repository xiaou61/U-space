package com.xiaou.moyu.domain;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 热榜API响应实体
 *
 * @author xiaou
 */
@Data
public class HotTopicResponse {
    
    /**
     * 所有类型映射
     */
    private Map<String, String> allTypes;
    
    /**
     * 分类列表
     */
    private List<HotTopicCategory> categories;
    
    /**
     * 消息
     */
    private String message;
    
    /**
     * 总数
     */
    private Integer total;
    
    /**
     * 使用说明
     */
    private String usage;
}
