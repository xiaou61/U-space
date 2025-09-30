package com.xiaou.chat.dto;

import lombok.Data;

import java.util.List;

/**
 * 历史消息响应DTO
 * 
 * @author xiaou
 */
@Data
public class ChatHistoryResponse {
    
    /**
     * 消息列表
     */
    private List<ChatMessageResponse> messages;
    
    /**
     * 是否还有更多
     */
    private Boolean hasMore;
}
