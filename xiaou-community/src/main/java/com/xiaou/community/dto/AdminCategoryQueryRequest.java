package com.xiaou.community.dto;

import com.xiaou.common.core.domain.PageRequest;
import lombok.Data;

/**
 * 管理端分类查询请求DTO
 * 
 * @author xiaou
 */
@Data
public class AdminCategoryQueryRequest implements PageRequest {
    
    /**
     * 页码
     */
    private Integer pageNum;
    
    /**
     * 每页大小
     */
    private Integer pageSize;
    
    /**
     * 关键字搜索（名称或描述）
     */
    private String keyword;
    
    /**
     * 状态筛选：1-启用，0-禁用
     */
    private Integer status;
    
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