package com.xiaou.community.dto;

import com.xiaou.common.core.domain.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 后台评论查询请求
 * 
 * @author xiaou
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AdminCommentQueryRequest implements PageRequest {
    
    /**
     * 帖子ID筛选
     */
    private Long postId;
    
    /**
     * 评论者筛选
     */
    private String authorName;
    
    /**
     * 状态筛选
     */
    private Integer status;
    
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
    public AdminCommentQueryRequest setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
        return this;
    }
    
    /**
     * 覆盖接口方法，支持链式调用
     */
    @Override
    public AdminCommentQueryRequest setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }
} 