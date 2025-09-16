package com.xiaou.knowledge.dto.request;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 创建知识节点请求
 * 
 * @author xiaou
 */
@Data
public class CreateKnowledgeNodeRequest {
    
    /**
     * 父节点ID，0为根节点
     */
    @NotNull(message = "父节点ID不能为空")
    private Long parentId;
    
    /**
     * 节点标题
     */
    @NotBlank(message = "节点标题不能为空")
    @Size(max = 200, message = "节点标题长度不能超过200个字符")
    private String title;
    
    /**
     * 节点详细内容(Markdown格式)
     */
    private String content;
    
    /**
     * 节点类型: 1-普通 2-重点 3-难点
     */
    private Integer nodeType = 1;
    
    /**
     * 同级排序序号
     */
    private Integer sortOrder = 0;
    
    /**
     * 是否默认展开
     */
    private Boolean isExpanded = true;
} 