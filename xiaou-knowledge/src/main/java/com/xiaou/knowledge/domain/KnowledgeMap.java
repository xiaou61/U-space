package com.xiaou.knowledge.domain;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;


import java.time.LocalDateTime;

/**
 * 知识图谱实体类
 * 
 * @author xiaou
 */
@Data
public class KnowledgeMap {
    
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
     * 创建用户ID(管理员)
     */
    private Long userId;
    
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
    
    /**
     * 状态枚举
     */
    public enum Status {
        DRAFT(0, "草稿"),
        PUBLISHED(1, "已发布"), 
        HIDDEN(2, "已隐藏");
        
        private final Integer code;
        private final String desc;
        
        Status(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public Integer getCode() {
            return code;
        }
        
        public String getDesc() {
            return desc;
        }
        
        public static Status of(Integer code) {
            if (code == null) {
                return null;
            }
            for (Status status : values()) {
                if (status.code.equals(code)) {
                    return status;
                }
            }
            return null;
        }
    }
} 