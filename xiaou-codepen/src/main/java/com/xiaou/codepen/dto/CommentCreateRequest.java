package com.xiaou.codepen.dto;

import lombok.Data;

/**
 * 评论发表请求
 * 
 * @author xiaou
 */
@Data
public class CommentCreateRequest {
    
    /**
     * 作品ID（必填）
     */
    private Long penId;
    
    /**
     * 评论内容（必填，1-500字符）
     */
    private String content;
    
    /**
     * 父评论ID（回复时传入）
     */
    private Long parentId;
    
    /**
     * 回复目标用户ID（回复时传入）
     */
    private Long replyToUserId;
}

