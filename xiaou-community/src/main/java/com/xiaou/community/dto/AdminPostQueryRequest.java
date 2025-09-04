package com.xiaou.community.dto;

import com.xiaou.common.core.domain.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 后台帖子查询请求
 * 
 * @author xiaou
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AdminPostQueryRequest implements PageRequest {
    
    /**
     * 状态筛选
     */
    private Integer status;
    
    /**
     * 作者筛选
     */
    private String authorName;
    
    /**
     * 分类筛选
     */
    private String category;
    
    /**
     * 关键词搜索
     */
    private String keyword;
    
    /**
     * 开始时间
     */
    private String startTime;
    
    /**
     * 结束时间
     */
    private String endTime;
    
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
    public AdminPostQueryRequest setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
        return this;
    }
    
    /**
     * 覆盖接口方法，支持链式调用
     */
    @Override
    public AdminPostQueryRequest setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }
} 