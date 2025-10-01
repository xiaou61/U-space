package com.xiaou.chat.dto;

import lombok.Data;

/**
 * 消息撤回请求DTO
 * 
 * @author xiaou
 */
@Data
public class ChatRecallRequest {
    
    /**
     * 消息ID
     */
    private Long messageId;
}
