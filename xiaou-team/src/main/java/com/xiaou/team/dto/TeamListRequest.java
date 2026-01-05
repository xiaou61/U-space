package com.xiaou.team.dto;

import lombok.Data;

/**
 * 小组列表查询请求DTO
 * 
 * @author xiaou
 */
@Data
public class TeamListRequest {
    
    /**
     * 当前页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页条数
     */
    private Integer pageSize = 10;
    
    /**
     * 小组类型筛选
     */
    private Integer teamType;
    
    /**
     * 标签筛选
     */
    private String tag;
    
    /**
     * 关键字搜索（小组名称）
     */
    private String keyword;
    
    /**
     * 排序方式：hot-热门 new-最新 active-活跃
     */
    private String sortBy = "hot";
    
    /**
     * 用户ID（用于查询我的小组）
     */
    private Long userId;
    
    /**
     * 只查询可加入的小组
     */
    private Boolean joinable;
}
