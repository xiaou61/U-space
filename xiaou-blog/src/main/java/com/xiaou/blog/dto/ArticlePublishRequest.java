package com.xiaou.blog.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 文章发布请求DTO
 * 
 * @author xiaou
 */
@Data
public class ArticlePublishRequest {
    
    /**
     * 文章ID（编辑时传入，新建时为空）
     */
    private Long id;
    
    /**
     * 文章标题
     */
    @NotBlank(message = "文章标题不能为空")
    @Size(min = 1, max = 100, message = "文章标题长度必须在1-100个字符之间")
    private String title;
    
    /**
     * 文章封面图
     */
    private String coverImage;
    
    /**
     * 文章摘要
     */
    @Size(max = 200, message = "文章摘要不能超过200个字符")
    private String summary;
    
    /**
     * 文章内容（Markdown格式）
     */
    @NotBlank(message = "文章内容不能为空")
    @Size(min = 50, message = "文章内容不能少于50个字符")
    private String content;
    
    /**
     * 文章分类ID
     */
    @NotNull(message = "文章分类不能为空")
    private Long categoryId;
    
    /**
     * 文章标签（最多5个）
     */
    @Size(max = 5, message = "标签数量不能超过5个")
    private List<String> tags;
    
    /**
     * 是否原创：1-原创 0-转载
     */
    private Integer isOriginal;
}


