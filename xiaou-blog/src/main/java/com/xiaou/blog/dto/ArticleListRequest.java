package com.xiaou.blog.dto;

import lombok.Data;

/**
 * 文章列表查询请求DTO
 * 
 * @author xiaou
 */
@Data
public class ArticleListRequest {
    
    /**
     * 用户ID（查询指定用户的文章）
     */
    private Long userId;
    
    /**
     * 分类ID
     */
    private Long categoryId;
    
    /**
     * 标签（支持多标签组合筛选）
     */
    private String tags;
    
    /**
     * 关键词搜索（标题、内容）
     */
    private String keyword;
    
    /**
     * 文章状态：0-草稿 1-已发布 2-已下架 3-已删除
     */
    private Integer status;
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页大小
     */
    private Integer pageSize = 10;
}


