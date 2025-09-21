package com.xiaou.version.dto;

import com.xiaou.common.core.domain.PageRequest;
import lombok.Data;

/**
 * 版本历史查询请求DTO
 * 
 * @author xiaou
 */
@Data
public class VersionHistoryQueryRequest implements PageRequest {
    
    /**
     * 关键词搜索（标题、版本号）
     */
    private String keyword;
    
    /**
     * 更新类型筛选: 1-重大更新, 2-功能更新, 3-修复更新, 4-其他
     */
    private Integer updateType;
    
    /**
     * 状态筛选: 0-草稿, 1-已发布, 2-已隐藏
     */
    private Integer status;
    
    /**
     * 是否重点推荐: 0-否, 1-是
     */
    private Integer isFeatured;
    
    /**
     * 发布时间开始
     */
    private String releaseTimeStart;
    
    /**
     * 发布时间结束
     */
    private String releaseTimeEnd;
    
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