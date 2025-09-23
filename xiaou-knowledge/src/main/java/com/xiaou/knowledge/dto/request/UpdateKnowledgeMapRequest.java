package com.xiaou.knowledge.dto.request;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 更新知识图谱请求
 * 
 * @author xiaou
 */
@Data
public class UpdateKnowledgeMapRequest {
    
    /**
     * 图谱标题
     */
    @NotBlank(message = "图谱标题不能为空")
    @Size(max = 200, message = "图谱标题长度不能超过200个字符")
    private String title;
    
    /**
     * 图谱描述
     */
    @Size(max = 2000, message = "图谱描述长度不能超过2000个字符")
    private String description;
    
    /**
     * 封面图片URL
     */
    @Size(max = 500, message = "封面图片URL长度不能超过500个字符")
    private String coverImage;
    
    /**
     * 排序权重
     */
    private Integer sortOrder;
} 