package com.xiaou.blog.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 博客配置响应DTO
 * 
 * @author xiaou
 */
@Data
public class BlogConfigResponse {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 博客名称
     */
    private String blogName;
    
    /**
     * 博客简介
     */
    private String blogDescription;
    
    /**
     * 博客头像
     */
    private String blogAvatar;
    
    /**
     * 博客背景图
     */
    private String blogCover;
    
    /**
     * 博客公告
     */
    private String blogNotice;
    
    /**
     * 个人标签
     */
    private List<String> personalTags;
    
    /**
     * 社交链接
     */
    private Map<String, String> socialLinks;
    
    /**
     * 文章总数
     */
    private Integer totalArticles;
}


