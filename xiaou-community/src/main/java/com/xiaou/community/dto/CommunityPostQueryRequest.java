package com.xiaou.community.dto;

import com.xiaou.common.core.domain.PageRequest;
import lombok.Data;

/**
 * 前台帖子查询请求DTO
 * 
 * @author xiaou
 */
@Data
public class CommunityPostQueryRequest implements PageRequest {
    
    /**
     * 分类ID筛选
     */
    private Long categoryId;
    
    /**
     * 标签ID筛选
     */
    private Long tagId;
    
    /**
     * 关键词搜索
     */
    private String keyword;
    
    /**
     * 排序方式：hot-热度排序，time-时间排序，默认时间排序
     */
    private String sortBy = "time";
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页大小
     */
    private Integer pageSize = 10;
    
    @Override
    public PageRequest setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
        return this;
    }
    
    @Override
    public PageRequest setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }
} 