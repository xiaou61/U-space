package com.xiaou.points.dto;

import lombok.Data;

/**
 * 用户积分列表查询请求DTO
 * 
 * @author xiaou
 */
@Data
public class UserPointsListRequest {
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页大小
     */
    private Integer pageSize = 20;
    
    /**
     * 用户名模糊搜索
     */
    private String userName;
    
    /**
     * 最小积分
     */
    private Integer minPoints;
    
    /**
     * 最大积分
     */
    private Integer maxPoints;
    
    /**
     * 排序字段：points-积分, create_time-创建时间
     */
    private String orderBy = "points";
    
    /**
     * 排序方向：asc-升序, desc-降序
     */
    private String orderDirection = "desc";
}
