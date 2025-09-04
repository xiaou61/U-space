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
     * 页码
     */
    private Integer pageNum;
    
    /**
     * 每页大小
     */
    private Integer pageSize;
    
    /**
     * 分类筛选
     */
    private String category;
    
    /**
     * 关键字搜索
     */
    private String keyword;
    
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