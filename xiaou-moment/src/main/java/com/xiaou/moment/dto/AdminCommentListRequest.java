package com.xiaou.moment.dto;

import com.xiaou.common.core.domain.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端评论列表查询请求
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AdminCommentListRequest implements PageRequest {
    
    /**
     * 动态ID
     */
    private Long momentId;
    
    /**
     * 用户昵称
     */
    private String userNickname;
    
    /**
     * 评论内容关键词
     */
    private String contentKeyword;
    
    /**
     * 状态筛选
     */
    private Integer status;
    
    /**
     * 开始时间
     */
    private String startDate;
    
    /**
     * 结束时间
     */
    private String endDate;
    
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
    public AdminCommentListRequest setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
        return this;
    }
    
    /**
     * 覆盖接口方法，支持链式调用
     */
    @Override
    public AdminCommentListRequest setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }
} 