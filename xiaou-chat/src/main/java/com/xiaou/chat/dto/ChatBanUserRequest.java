package com.xiaou.chat.dto;

import lombok.Data;

/**
 * 禁言用户请求DTO
 * 
 * @author xiaou
 */
@Data
public class ChatBanUserRequest {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 禁言原因
     */
    private String banReason;
    
    /**
     * 禁言时长（秒），0表示永久
     */
    private Integer banDuration;
}
