package com.xiaou.community.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建帖子请求DTO
 * 
 * @author xiaou
 */
@Data
public class CommunityPostCreateRequest {
    
    /**
     * 帖子标题
     */
    @NotBlank(message = "帖子标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200个字符")
    private String title;
    
    /**
     * 帖子内容
     */
    @NotBlank(message = "帖子内容不能为空")
    private String content;
    
    /**
     * 分类ID（可选）
     */
    private Long categoryId;
    
    /**
     * 标签ID列表（可选，最多5个）
     */
    private java.util.List<Long> tagIds;
} 