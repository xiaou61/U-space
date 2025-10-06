package com.xiaou.community.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

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
    
    /**
     * 回复的评论ID
     */
    private Long replyToId;
    
    /**
     * 回复的用户ID
     */
    private Long replyToUserId;
    
    /**
     * 回复的用户名
     */
    private String replyToUserName;
    
    /**
     * 回复数量（仅一级评论有效）
     */
    private Integer replyCount;
    
    /**
     * 二级回复列表（最多展示2条）
     */
    private List<CommunityCommentResponse> replies;
    
    /**
     * 是否有更多回复
     */
    private Boolean hasMoreReplies;
} 