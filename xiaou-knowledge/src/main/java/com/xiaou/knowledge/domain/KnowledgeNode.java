package com.xiaou.knowledge.domain;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;


import java.time.LocalDateTime;

/**
 * 知识节点实体类
 * 
 * @author xiaou
 */
@Data

public class KnowledgeNode  {
    
    /**
     * 节点ID
     */
    private Long id;
    
    /**
     * 所属图谱ID
     */
    private Long mapId;
    
    /**
     * 父节点ID，0为根节点
     */
    private Long parentId;
    
    /**
     * 节点标题
     */
    private String title;
    
    /**
     * 节点详细内容(Markdown格式)
     */
    private String content;
    
    /**
     * 节点类型: 1-普通 2-重点 3-难点
     */
    private Integer nodeType;
    
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
     * 节点类型枚举
     */
    public enum NodeType {
        NORMAL(1, "普通"),
        IMPORTANT(2, "重点"),
        DIFFICULT(3, "难点");
        
        private final Integer code;
        private final String desc;
        
        NodeType(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public Integer getCode() {
            return code;
        }
        
        public String getDesc() {
            return desc;
        }
        
        public static NodeType of(Integer code) {
            if (code == null) {
                return null;
            }
            for (NodeType nodeType : values()) {
                if (nodeType.code.equals(code)) {
                    return nodeType;
                }
            }
            return null;
        }
    }
} 