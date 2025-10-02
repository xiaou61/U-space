package com.xiaou.blog.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 博客配置实体
 * 
 * @author xiaou
 */
@Data
public class BlogConfig {
    
    /**
     * 主键ID
     */
    private Long id;
    
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
    private String blogNotice;
    
    /**
     * 个人标签，JSON格式
     */
    private String personalTags;
    
    /**
     * 社交链接，JSON格式
     */
    private String socialLinks;
    
    /**
     * 是否公开：1-公开 0-私密
     */
    private Integer isPublic;
    
    /**
     * 文章总数
     */
    private Integer totalArticles;
    
    /**
     * 开通消耗积分
     */
    private Integer pointsCost;
    
    /**
     * 博客状态：1-正常 0-封禁
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;
}

