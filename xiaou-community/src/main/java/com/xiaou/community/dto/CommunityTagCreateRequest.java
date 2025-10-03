package com.xiaou.community.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建标签请求
 * 
 * @author xiaou
 */
@Data
public class CommunityTagCreateRequest {
    
    /**
     * 标签名称
     */
    @NotBlank(message = "标签名称不能为空")
    @Size(max = 50, message = "标签名称长度不能超过50个字符")
    private String name;
    
    /**
     * 标签描述
     */
    @Size(max = 200, message = "标签描述长度不能超过200个字符")
    private String description;
    
    /**
     * 标签颜色
     */
    private String color;
    
    /**
     * 标签图标
     */
    private String icon;
    
    /**
     * 排序顺序
     */
    private Integer sortOrder;
}

