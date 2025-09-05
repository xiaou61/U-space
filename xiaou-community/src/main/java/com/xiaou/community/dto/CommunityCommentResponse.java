package com.xiaou.community.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 评论响应DTO
 * 
 * @author xiaou
 */
@Data
public class CommunityCommentResponse {
    
    /**
     * 评论ID
     */
    private Long id;
    
    /**
     * 帖子ID
     */
    private Long postId;
    
    /**
     * 父评论ID，0表示顶级评论
     */
    private Long parentId;
    
    /**
     * 评论内容
     */
    private String content;
    
    /**
     * 评论者用户ID
     */
    private Long authorId;
    
    /**
     * 评论者用户名
     */
    private String authorName;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    
    /**
     * 当前用户是否点赞此评论
     */
    private Boolean isLiked;
} 