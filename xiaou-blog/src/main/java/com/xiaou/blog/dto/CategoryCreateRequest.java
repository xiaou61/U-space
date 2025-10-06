package com.xiaou.blog.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建分类请求DTO
 * 
 * @author xiaou
 */
@Data
public class CategoryCreateRequest {
    
    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称不能超过50个字符")
    private String categoryName;
    
    /**
     * 分类图标URL
     */
    private String categoryIcon;
    
    /**
     * 分类描述
     */
    @Size(max = 200, message = "分类描述不能超过200个字符")
    private String categoryDescription;
    
    /**
     * 排序权重
     */
    private Integer sortOrder;
}


