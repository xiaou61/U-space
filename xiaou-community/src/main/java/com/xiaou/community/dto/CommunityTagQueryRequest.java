package com.xiaou.community.dto;

import com.xiaou.common.core.domain.PageRequest;
import lombok.Data;

/**
 * 标签查询请求（后台管理）
 * 
 * @author xiaou
 */
@Data
public class CommunityTagQueryRequest implements PageRequest {
    
    /**
     * 页码
     */
    private Integer pageNum;
    
    /**
     * 每页大小
     */
    private Integer pageSize;
    
    /**
     * 关键词（名称或描述）
     */
    private String keyword;
    
    /**
     * 状态：1-启用，0-禁用
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

