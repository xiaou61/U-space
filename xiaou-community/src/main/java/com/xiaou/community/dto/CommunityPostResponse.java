package com.xiaou.community.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaou.community.domain.CommunityTag;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 前台帖子响应DTO
 * 
 * @author xiaou
 */
@Data
public class CommunityPostResponse {
    
    /**
     * 帖子ID
     */
    private Long id;
    
    /**
     * 帖子标题
     */
    private String title;
    
    /**
     * 帖子内容
     */
    private String content;
    
    /**
     * 分类ID
     */
    private Long categoryId;
    
    /**
     * 分类名称
     */
    private String categoryName;
    
    /**
     * 作者用户ID
     */
    private Long authorId;
    
    /**
     * 作者用户名
     */
    private String authorName;
    
    /**
     * 浏览次数
     */
    private Integer viewCount;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 评论数
     */
    private Integer commentCount;
    
    /**
     * 收藏数
     */
    private Integer collectCount;
    
    /**
     * 是否置顶
     */
    private Boolean isTop;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    
    /**
     * 当前用户是否点赞
     */
    private Boolean isLiked;
    
    /**
     * 当前用户是否收藏
     */
    private Boolean isCollected;
    
    /**
     * 标签列表
     */
    private List<CommunityTag> tags;
    
    /**
     * AI生成的摘要
     */
    private String aiSummary;
    
    /**
     * 热度分数（用于排序）
     */
    private Double hotScore;
} 