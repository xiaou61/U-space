package com.xiaou.moment.dto;

import com.xiaou.common.core.domain.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户端动态列表查询请求
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class UserMomentListRequest implements PageRequest {
    
    /**
     * 页码
     */
    private Integer pageNum = 1;
    
    /**
     * 每页大小
     */
    private Integer pageSize = 10;
    
    /**
     * 覆盖接口方法，支持链式调用
     */
    @Override
    public UserMomentListRequest setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
        return this;
    }
    
    /**
     * 覆盖接口方法，支持链式调用
     */
    @Override
    public UserMomentListRequest setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }
} 