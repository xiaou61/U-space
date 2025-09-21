package com.xiaou.version.dto;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 版本历史创建请求DTO
 * 
 * @author xiaou
 */
@Data
public class VersionHistoryCreateRequest {
    
    /**
     * 版本号
     */
    @NotBlank(message = "版本号不能为空")
    @Length(max = 50, message = "版本号长度不能超过50个字符")
    private String versionNumber;
    
    /**
     * 更新标题
     */
    @NotBlank(message = "更新标题不能为空")
    @Length(max = 200, message = "更新标题长度不能超过200个字符")
    private String title;
    
    /**
     * 更新类型: 1-重大更新, 2-功能更新, 3-修复更新, 4-其他
     */
    @NotNull(message = "更新类型不能为空")
    private Integer updateType;
    
    /**
     * 版本更新简要描述
     */
    @Length(max = 500, message = "描述长度不能超过500个字符")
    private String description;
    
    /**
     * PRD文档链接
     */
    @Length(max = 500, message = "PRD链接长度不能超过500个字符")
    private String prdUrl;
    
    /**
     * 发布时间
     */
    @NotBlank(message = "发布时间不能为空")
    private String releaseTime;
    
    /**
     * 状态: 0-草稿, 1-已发布, 2-已隐藏
     */
    private Integer status = 0;
    
    /**
     * 排序权重(数字越大越靠前)
     */
    private Integer sortOrder = 0;
    
    /**
     * 是否重点推荐: 0-否, 1-是
     */
    private Integer isFeatured = 0;
} 