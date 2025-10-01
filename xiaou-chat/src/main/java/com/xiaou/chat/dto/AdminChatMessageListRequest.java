package com.xiaou.chat.dto;

import com.xiaou.common.core.domain.PageRequest;
import lombok.Data;

/**
 * 管理端消息查询请求DTO
 * 
 * @author xiaou
 */
@Data
public class AdminChatMessageListRequest implements PageRequest {
    
    /**
     * 用户ID筛选
     */
    private Long userId;
    
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
    private Integer pageSize = 20;
    
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
