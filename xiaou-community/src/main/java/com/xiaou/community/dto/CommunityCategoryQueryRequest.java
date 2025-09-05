package com.xiaou.community.dto;

import com.xiaou.common.core.domain.PageRequest;
import lombok.Data;

/**
 * 前台分类查询请求DTO
 * 
 * @author xiaou
 */
@Data
public class CommunityCategoryQueryRequest implements PageRequest {
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页大小
     */
    private Integer pageSize = 10;
    
    /**
     * 关键字搜索（名称或描述）
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