package com.xiaou.moyu.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Bug条目实体
 *
 * @author xiaou
 */
@Data
public class BugItem {
    
    /**
     * Bug ID
     */
    private Long id;
    
    /**
     * Bug标题
     */
    private String title;
    
    /**
     * Bug现象描述
     */
    private String phenomenon;
    
    /**
     * 原因分析
     */
    private String causeAnalysis;
    
    /**
     * 解决方案
     */
    private String solution;
    
    /**
     * 技术标签（多个标签用逗号分隔）
     */
    private String techTags;
    
    /**
     * 难度等级：1-初级，2-中级，3-高级，4-专家级
     */
    private Integer difficultyLevel;
    
    /**
     * 难度等级描述
     */
    private String difficultyName;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 排序值（数字越大越靠前）
     */
    private Integer sortOrder;
    
    /**
     * 创建者ID
     */
    private Long createdBy;
    
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
     * 获取难度等级描述
     */
    public String getDifficultyName() {
        if (difficultyLevel == null) {
            return "未知";
        }
        return switch (difficultyLevel) {
            case 1 -> "初级";
            case 2 -> "中级";
            case 3 -> "高级";
            case 4 -> "专家级";
            default -> "未知";
        };
    }
}
