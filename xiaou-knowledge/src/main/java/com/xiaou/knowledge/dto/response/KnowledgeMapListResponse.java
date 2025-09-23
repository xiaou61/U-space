package com.xiaou.knowledge.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识图谱列表响应
 * 
 * @author xiaou
 */
@Data
public class KnowledgeMapListResponse {
    
    /**
     * 图谱ID
     */
    private Long id;
    
    /**
     * 图谱标题
     */
    private String title;
    
    /**
     * 图谱描述
     */
    private String description;
    
    /**
     * 封面图片URL
     */
    private String coverImage;
    
    /**
     * 节点总数
     */
    private Integer nodeCount;
    
    /**
     * 查看次数
     */
    private Integer viewCount;
    
    /**
     * 状态: 0-草稿 1-已发布 2-已隐藏
     */
    private Integer status;
    
    /**
     * 状态描述
     */
    private String statusDesc;
    
    /**
     * 排序权重
     */
    private Integer sortOrder;
    
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
} 