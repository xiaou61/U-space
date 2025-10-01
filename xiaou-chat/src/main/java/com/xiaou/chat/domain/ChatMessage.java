package com.xiaou.chat.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 聊天消息实体
 * 
 * @author xiaou
 */
@Data
public class ChatMessage {
    
    /**
     * 消息ID
     */
    private Long id;
    
    /**
     * 聊天室ID
     */
    private Long roomId;
    
    /**
     * 发送者用户ID
     */
    private Long userId;
    
    /**
     * 用户昵称（查询时使用）
     */
    private String userNickname;
    
    /**
     * 用户头像（查询时使用）
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
     * 图片URL（消息类型为图片时）
     */
    private String imageUrl;
    
    /**
     * 是否删除：0否 1是
     */
    private Integer isDeleted;
    
    /**
     * 发送者IP地址
     */
    private String ipAddress;
    
    /**
     * 设备信息
     */
    private String deviceInfo;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
}
