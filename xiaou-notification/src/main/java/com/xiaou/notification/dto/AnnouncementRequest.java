package com.xiaou.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;



/**
 * 系统公告发布请求DTO
 */
@Data
public class AnnouncementRequest {
    
    /**
     * 公告标题
     */
    @NotBlank(message = "公告标题不能为空")
    @Size(max = 200, message = "公告标题长度不能超过200字符")
    private String title;
    
    /**
     * 公告内容
     */
    @NotBlank(message = "公告内容不能为空")
    private String content;
    
    /**
     * 优先级：HIGH/MEDIUM/LOW
     */
    private String priority = "LOW";
} 