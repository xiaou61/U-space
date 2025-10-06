package com.xiaou.community.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 回复评论请求
 * 
 * @author xiaou
 */
@Data
public class CommunityCommentReplyRequest {
    
    /**
     * 回复内容
     */
    @NotBlank(message = "回复内容不能为空")
    @Size(min = 1, max = 500, message = "回复内容为1-500个字符")
    private String content;
    
    /**
     * 回复的评论ID
     */
    @NotNull(message = "回复的评论ID不能为空")
    private Long replyToId;
    
    /**
     * 回复的用户ID
     */
    @NotNull(message = "回复的用户ID不能为空")
    private Long replyToUserId;
}

