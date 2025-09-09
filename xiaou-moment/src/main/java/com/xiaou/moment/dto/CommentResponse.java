package com.xiaou.moment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评论响应
 */
@Data
public class CommentResponse {
    
    /**
     * 评论ID
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
     * 评论内容
     */
    private String content;
    
    /**
     * 是否可删除
     */
    private Boolean canDelete;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
} 