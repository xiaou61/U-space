package com.xiaou.blog.dto;

import lombok.Data;

/**
 * 博客统计响应DTO
 * 
 * @author xiaou
 */
@Data
public class BlogStatisticsResponse {
    
    /**
     * 博客总数
     */
    private Long totalBlogs;
    
    /**
     * 文章总数
     */
    private Long totalArticles;
    
    /**
     * 活跃博主数（近30天发布过文章）
     */
    private Long activeBloggers;
}


