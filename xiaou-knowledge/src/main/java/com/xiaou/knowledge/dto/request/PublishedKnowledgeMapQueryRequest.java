package com.xiaou.knowledge.dto.request;

import com.xiaou.common.core.domain.PageRequest;
import lombok.Data;

/**
 * 用户端知识图谱查询请求DTO（仅查询已发布的图谱）
 * 
 * @author xiaou
 */
@Data
public class PublishedKnowledgeMapQueryRequest implements PageRequest {
    
    /**
     * 搜索关键词
     */
    private String keyword;
    
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