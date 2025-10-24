package com.xiaou.codepen.dto;

import lombok.Data;

import java.util.List;

/**
 * 作品列表查询请求
 * 
 * @author xiaou
 */
@Data
public class CodePenListRequest {
    
    /**
     * 作者ID
     */
    private Long userId;
    
    /**
     * 分类
     */
    private String category;
    
    /**
     * 标签筛选
     */
    private List<String> tags;
    
    /**
     * 关键词搜索
     */
    private String keyword;
    
    /**
     * 是否免费筛选：1-免费 0-付费
     */
    private Integer isFree;
    
    /**
     * 排序方式：latest/hot/most_liked/most_viewed，默认latest
     */
    private String sortBy;
    
    /**
     * 状态筛选
     */
    private Integer status;
    
    /**
     * 页码，默认1
     */
    private Integer pageNum;
    
    /**
     * 每页大小，默认20
     */
    private Integer pageSize;
}

