package com.xiaou.community.dto;

import com.xiaou.common.core.domain.PageRequest;
import lombok.Data;

/**
 * 评论查询请求DTO
 * 
 * @author xiaou
 */
@Data
public class CommunityCommentQueryRequest implements PageRequest {
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页大小
     */
    private Integer pageSize = 10;
    
    /**
     * 排序方式：time/hot，默认time
     */
    private String sort = "time";
    
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