package com.xiaou.moyu.dto;

import lombok.Data;

/**
 * Bug条目响应DTO
 *
 * @author xiaou
 */
@Data
public class BugItemDto {
    
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
     * 技术标签数组
     */
    private String[] techTags;
    
    /**
     * 难度等级
     */
    private Integer difficultyLevel;
    
    /**
     * 难度等级描述
     */
    private String difficultyName;
}
