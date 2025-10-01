package com.xiaou.chat.dto;

import lombok.Data;

/**
 * 系统公告请求DTO
 * 
 * @author xiaou
 */
@Data
public class ChatAnnouncementRequest {
    
    /**
     * 公告内容
     */
    private String content;
}
