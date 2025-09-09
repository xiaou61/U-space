package com.xiaou.moment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

/**
 * 发布评论请求
 */
@Data
public class CommentPublishRequest {
    
    /**
     * 动态ID
     */
    @NotNull(message = "动态ID不能为空")
    private Long momentId;
    
    /**
     * 评论内容
     */
    @NotBlank(message = "评论内容不能为空")
    @Length(min = 1, max = 200, message = "评论内容长度需在1-200字符之间")
    private String content;
} 