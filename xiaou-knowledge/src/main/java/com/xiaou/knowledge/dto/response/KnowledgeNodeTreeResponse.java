package com.xiaou.knowledge.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 知识节点树形响应
 * 
 * @author xiaou
 */
@Data
public class KnowledgeNodeTreeResponse {
    
    /**
     * 节点ID
     */
    private Long id;
    
    /**
     * 所属图谱ID
     */
    private Long mapId;
    
    /**
     * 父节点ID
     */
    private Long parentId;
    
    /**
     * 节点标题
     */
    private String title;
    
    /**
     * 飞书云文档链接
     */
    private String url;
    
    /**
     * 节点类型: 1-普通 2-重点 3-难点
     */
    private Integer nodeType;
    
    /**
     * 节点类型描述
     */
    private String nodeTypeDesc;
    
    /**
     * 同级排序序号
     */
    private Integer sortOrder;
    
    /**
     * 层级深度
     */
    private Integer levelDepth;
    
    /**
     * 是否默认展开
     */
    private Boolean isExpanded;
    
    /**
     * 查看次数
     */
    private Integer viewCount;
    
    /**
     * 最后查看时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastViewTime;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
    
    /**
     * 子节点列表
     */
    private List<KnowledgeNodeTreeResponse> children;
} 