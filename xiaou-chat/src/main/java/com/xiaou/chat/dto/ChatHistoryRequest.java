package com.xiaou.chat.dto;

import lombok.Data;

/**
 * 历史消息查询请求DTO
 * 
 * @author xiaou
 */
@Data
public class ChatHistoryRequest {
    
    /**
     * 最后一条消息ID，0表示最新
     */
    private Long lastMessageId = 0L;
    
    /**
     * 每页大小，默认20
     */
    private Integer pageSize = 20;
}
