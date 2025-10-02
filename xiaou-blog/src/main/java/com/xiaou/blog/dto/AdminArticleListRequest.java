package com.xiaou.blog.dto;

import lombok.Data;

/**
 * 管理端文章列表查询请求DTO
 * 
 * @author xiaou
 */
@Data
public class AdminArticleListRequest {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 分类ID
     */
    private Long categoryId;
    
    /**
     * 文章状态
     */
    private Integer status;
    
    /**
     * 关键词搜索
     */
    private String keyword;
    
    /**
     * 开始时间
     */
    private String startTime;
    
    /**
     * 结束时间
     */
    private String endTime;
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页大小
     */
    private Integer pageSize = 20;
}


