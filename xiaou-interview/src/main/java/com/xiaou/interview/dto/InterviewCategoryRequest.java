package com.xiaou.interview.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 面试题分类请求DTO
 *
 * @author xiaou
 */
@Data
public class InterviewCategoryRequest {

    /**
     * 分类名称
     */
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 100, message = "分类名称长度不能超过100字符")
    private String name;

    /**
     * 分类描述
     */
    @Size(max = 500, message = "分类描述长度不能超过500字符")
    private String description;

    /**
     * 排序序号
     */
    private Integer sortOrder;

    /**
     * 状态 (0-禁用 1-启用)
     */
    private Integer status;
} 