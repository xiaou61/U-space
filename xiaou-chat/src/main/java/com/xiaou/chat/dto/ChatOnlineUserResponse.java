package com.xiaou.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 在线用户响应DTO
 * 
 * @author xiaou
 */
@Data
public class ChatOnlineUserResponse {
    
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
     * 连接时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date connectTime;
    
    /**
     * IP地址
     */
    private String ipAddress;
    
    /**
     * 设备信息
     */
    private String deviceInfo;
}
