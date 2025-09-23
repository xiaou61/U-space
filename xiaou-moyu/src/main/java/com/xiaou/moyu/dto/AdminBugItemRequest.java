package com.xiaou.moyu.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * 管理端Bug条目请求DTO
 *
 * @author xiaou
 */
@Data
public class AdminBugItemRequest {
    
    /**
     * Bug ID（更新时必须）
     */
    private Long id;
    
    /**
     * Bug标题
     */
    @NotBlank(message = "Bug标题不能为空")
    private String title;
    
    /**
     * Bug现象描述
     */
    @NotBlank(message = "Bug现象描述不能为空")
    private String phenomenon;
    
    /**
     * 原因分析
     */
    @NotBlank(message = "原因分析不能为空")
    private String causeAnalysis;
    
    /**
     * 解决方案
     */
    @NotBlank(message = "解决方案不能为空")
    private String solution;
    
    /**
     * 技术标签（多个标签用逗号分隔）
     */
    private String techTags;
    
    /**
     * 难度等级：1-初级，2-中级，3-高级，4-专家级
     */
    @NotNull(message = "难度等级不能为空")
    @Min(value = 1, message = "难度等级最小为1")
    @Max(value = 4, message = "难度等级最大为4")
    private Integer difficultyLevel;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 排序值
     */
    private Integer sortOrder;
}
