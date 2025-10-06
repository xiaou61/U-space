package com.xiaou.blog.dto;

import lombok.Data;

import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;

/**
 * 博客配置更新请求DTO
 * 
 * @author xiaou
 */
@Data
public class BlogConfigUpdateRequest {
    
    /**
     * 博客名称
     */
    @Size(max = 100, message = "博客名称不能超过100个字符")
    private String blogName;
    
    /**
     * 博客简介
     */
    @Size(max = 200, message = "博客简介不能超过200个字符")
    private String blogDescription;
    
    /**
     * 博客头像URL
     */
    private String blogAvatar;
    
    /**
     * 博客背景图URL
     */
    private String blogCover;
    
    /**
     * 博客公告
     */
    @Size(max = 100, message = "博客公告不能超过100个字符")
    private String blogNotice;
    
    /**
     * 个人标签（最多10个）
     */
    private List<String> personalTags;
    
    /**
     * 社交链接（Github、CSDN、掘金等）
     */
    private Map<String, String> socialLinks;
    
    /**
     * 是否公开：1-公开 0-私密
     */
    private Integer isPublic;
}


