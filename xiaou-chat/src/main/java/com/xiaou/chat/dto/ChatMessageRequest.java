package com.xiaou.chat.dto;

import lombok.Data;

/**
 * 聊天消息发送请求DTO
 * 
 * @author xiaou
 */
@Data
public class ChatMessageRequest {
    
    /**
     * 消息类型：1文本 2图片
     */
    private Integer messageType;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 图片URL（消息类型为图片时）
     */
    private String imageUrl;
    
    /**
     * 回复的消息ID
     */
    private Long replyToId;
    
    /**
     * 临时ID（前端乐观更新用）
     */
    private String tempId;
}
