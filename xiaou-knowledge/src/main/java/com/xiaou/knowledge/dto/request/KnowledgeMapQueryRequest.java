package com.xiaou.knowledge.dto.request;

import com.xiaou.common.core.domain.PageRequest;
import lombok.Data;

/**
 * 知识图谱查询请求DTO
 * 
 * @author xiaou
 */
@Data
public class KnowledgeMapQueryRequest implements PageRequest {
    
    /**
     * 搜索关键词
     */
    private String keyword;
    
    /**
     * 状态筛选: 0-草稿 1-已发布 2-已隐藏
     */
    private Integer status;
    
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