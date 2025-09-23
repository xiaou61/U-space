package com.xiaou.moyu.dto;

import lombok.Data;

/**
 * Bug条目查询请求DTO
 *
 * @author xiaou
 */
@Data
public class BugItemQueryRequest {
    
    /**
     * 当前页
     */
    private Integer current = 1;
    
    /**
     * 每页大小
     */
    private Integer size = 10;
    
    /**
     * Bug标题（模糊查询）
     */
    private String title;
    
    /**
     * 难度等级
     */
    private Integer difficultyLevel;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 技术标签（模糊查询）
     */
    private String techTag;
}
