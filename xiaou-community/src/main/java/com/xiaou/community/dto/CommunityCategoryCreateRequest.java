package com.xiaou.community.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * 创建分类请求DTO
 * 
 * @author xiaou
 */
@Data
public class CommunityCategoryCreateRequest {
    
    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不能超过50个字符")
    private String name;
    
    /**
     * 分类描述
     */
    @Size(max = 200, message = "分类描述长度不能超过200个字符")
    private String description;
    
    /**
     * 排序顺序
     */
    @Min(value = 0, message = "排序顺序不能小于0")
    @Max(value = 999, message = "排序顺序不能大于999")
    private Integer sortOrder = 0;
} 