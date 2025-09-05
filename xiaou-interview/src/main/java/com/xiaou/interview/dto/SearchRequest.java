package com.xiaou.interview.dto;

import lombok.Data;

/**
 * 搜索请求DTO
 * 
 * @author xiaou
 */
@Data
public class SearchRequest {
    
    /**
     * 搜索关键词
     */
    private String keyword;
    
    /**
     * 页码
     */
    private Integer page = 1;
    
    /**
     * 每页大小
     */
    private Integer size = 10;
} 