package com.xiaou.plan.dto;

import lombok.Data;

/**
 * 计划列表查询请求DTO
 * 
 * @author xiaou
 */
@Data
public class PlanListRequest {
    
    /**
     * 用户ID（内部使用）
     */
    private Long userId;
    
    /**
     * 计划状态筛选
     */
    private Integer status;
    
    /**
     * 计划类型筛选
     */
    private Integer planType;
    
    /**
     * 搜索关键字
     */
    private String keyword;
    
    /**
     * 当前页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页数量
     */
    private Integer pageSize = 10;
}
