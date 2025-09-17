package com.xiaou.knowledge.dto.request;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 更新知识节点请求
 * 
 * @author xiaou
 */
@Data
public class UpdateKnowledgeNodeRequest {
    
    /**
     * 节点标题
     */
    @NotBlank(message = "节点标题不能为空")
    @Size(max = 200, message = "节点标题长度不能超过200个字符")
    private String title;
    
    /**
     * 飞书云文档链接
     */
    @Size(max = 1000, message = "飞书文档链接长度不能超过1000个字符")
    private String url;
    
    /**
     * 节点类型: 1-普通 2-重点 3-难点
     */
    private Integer nodeType;
    
    /**
     * 是否默认展开
     */
    private Boolean isExpanded;
} 