package com.xiaou.community.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;

/**
 * 创建评论请求DTO
 * 
 * @author xiaou
 */
@Data
public class CommunityCommentCreateRequest {
    
    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    private String content;
    
    /**
     * 父评论ID（可选，0表示顶级评论）
     */
    private Long parentId = 0L;
} 