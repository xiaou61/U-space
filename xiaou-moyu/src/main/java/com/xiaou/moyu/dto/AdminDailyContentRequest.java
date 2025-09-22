package com.xiaou.moyu.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;

/**
 * 管理后台每日内容请求对象
 *
 * @author xiaou
 */
@Data
public class AdminDailyContentRequest {
    
    /**
     * 内容ID（更新时使用）
     */
    private Long id;
    
    /**
     * 内容类型：1-编程格言，2-技术小贴士，3-代码片段，4-历史上的今天
     */
    @NotNull(message = "内容类型不能为空")
    @Min(value = 1, message = "内容类型值无效")
    @Max(value = 4, message = "内容类型值无效")
    private Integer contentType;
    
    /**
     * 内容标题
     */
    private String title;
    
    /**
     * 内容详情
     */
    @NotBlank(message = "内容详情不能为空")
    private String content;
    
    /**
     * 作者
     */
    private String author;
    
    /**
     * 编程语言（代码片段专用）
     */
    private String programmingLanguage;
    
    /**
     * 标签列表
     */
    private List<String> tags;
    
    /**
     * 难度等级：1-初级，2-中级，3-高级
     */
    @Min(value = 1, message = "难度等级值无效")
    @Max(value = 3, message = "难度等级值无效")
    private Integer difficultyLevel;
    
    /**
     * 来源链接
     */
    private String sourceUrl;
    
    /**
     * 状态：0-禁用，1-启用
     */
    @Min(value = 0, message = "状态值无效")
    @Max(value = 1, message = "状态值无效")
    private Integer status;
}
