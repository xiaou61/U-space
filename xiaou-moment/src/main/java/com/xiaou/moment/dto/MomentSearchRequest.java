package com.xiaou.moment.dto;

import lombok.Data;

/**
 * 动态搜索请求DTO
 */
@Data
public class MomentSearchRequest {
    
    /**
     * 搜索关键词
     */
    private String keyword;
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页大小
     */
    private Integer pageSize = 20;
}

