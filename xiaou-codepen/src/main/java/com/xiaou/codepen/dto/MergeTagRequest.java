package com.xiaou.codepen.dto;

import lombok.Data;

/**
 * 合并标签请求
 * 
 * @author xiaou
 */
@Data
public class MergeTagRequest {
    
    /**
     * 源标签ID（将被合并）
     */
    private Long sourceId;
    
    /**
     * 目标标签ID（保留）
     */
    private Long targetId;
}

