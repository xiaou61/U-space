package com.xiaou.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 聊天消息响应DTO
 * 
 * @author xiaou
 */
@Data
public class ChatMessageResponse {
    
    /**
     * 消息ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户昵称
     */
    private String userNickname;
    
    /**
     * 用户头像URL
     */
    private String userAvatar;
    
    /**
     * 消息类型：1文本 2图片 3系统消息
     */
    private Integer messageType;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 图片URL
     */
    private String imageUrl;
    
    /**
     * 回复的消息ID
     */
    private Long replyToId;
    
    /**
     * 被回复消息内容摘要
     */
    private String replyToContent;
    
    /**
     * 被回复者昵称
     */
    private String replyToUser;
    
    /**
     * 是否可撤回
     */
    private Boolean canRecall;
    
    /**
     * 临时ID（前端乐观更新用）
     */
    private String tempId;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
