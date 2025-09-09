package com.xiaou.moment.dto;

import com.xiaou.common.core.domain.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端动态列表查询请求
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class AdminMomentListRequest implements PageRequest {
    
    /**
     * 用户昵称
     */
    private String userNickname;
    
    /**
     * 状态筛选
     */
    private Integer status;
    
    /**
     * 是否包含敏感词
     */
    private Boolean hasSensitiveWord;
    
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
    public AdminMomentListRequest setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
        return this;
    }
    
    /**
     * 覆盖接口方法，支持链式调用
     */
    @Override
    public AdminMomentListRequest setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }
} 