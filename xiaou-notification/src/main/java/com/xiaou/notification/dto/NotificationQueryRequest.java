package com.xiaou.notification.dto;

import com.xiaou.common.core.domain.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 消息通知查询请求DTO
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class NotificationQueryRequest implements PageRequest {
    
    /**
     * 消息状态：UNREAD/read/DELETED
     */
    private String status;
    
    /**
     * 消息类型：PERSONAL/ANNOUNCEMENT/COMMUNITY_INTERACTION/SYSTEM
     */
    private String type;
    
    /**
     * 消息标题（模糊查询）
     */
    private String title;
    
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
    
    @Override
    public Integer getPageNum() {
        return pageNum;
    }
    
    @Override
    public Integer getPageSize() {
        return pageSize;
    }
    
    @Override
    public NotificationQueryRequest setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
        return this;
    }
    
    @Override
    public NotificationQueryRequest setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
        return this;
    }
} 