package com.xiaou.moment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 动态列表响应
 */
@Data
public class MomentListResponse {
    
    /**
     * 动态ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户昵称
     */
    private String userNickname;
    
    /**
     * 用户头像
     */
    private String userAvatar;
    
    /**
     * 动态内容
     */
    private String content;
    
    /**
     * 图片URLs
     */
    private List<String> images;
    
    /**
     * 点赞数
     */
    private Integer likeCount;
    
    /**
     * 评论数
     */
    private Integer commentCount;
    
    /**
     * 当前用户是否已点赞
     */
    private Boolean isLiked;
    
    /**
     * 当前用户是否可删除
     */
    private Boolean canDelete;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    
    /**
     * 最新评论列表
     */
    private List<CommentResponse> recentComments;
} 