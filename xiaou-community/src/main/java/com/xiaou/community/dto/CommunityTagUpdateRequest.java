package com.xiaou.community.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 更新标签请求
 * 
 * @author xiaou
 */
@Data
public class CommunityTagUpdateRequest {
    
    /**
     * 标签ID
     */
    @NotNull(message = "标签ID不能为空")
    private Long id;
    
    /**
     * 标签名称
     */
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
    
    /**
     * 状态：1-启用，0-禁用
     */
    private Integer status;
}

