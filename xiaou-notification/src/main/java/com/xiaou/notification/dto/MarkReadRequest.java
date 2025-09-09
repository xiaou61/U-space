package com.xiaou.notification.dto;

import lombok.Data;

import java.util.List;

/**
 * 标记已读请求DTO
 */
@Data
public class MarkReadRequest {
    
    /**
     * 消息ID（单个标记已读）
     */
    private Long messageId;
    
    /**
     * 消息ID列表（批量标记已读）
     */
    private List<Long> messageIds;
    
    /**
     * 是否标记全部已读
     */
    private Boolean markAllRead = false;
} 