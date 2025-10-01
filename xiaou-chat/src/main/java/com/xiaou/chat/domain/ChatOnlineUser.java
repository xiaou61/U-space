package com.xiaou.chat.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 在线用户实体
 * 
 * @author xiaou
 */
@Data
public class ChatOnlineUser {
    
    /**
     * 记录ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 聊天室ID
     */
    private Long roomId;
    
    /**
     * WebSocket会话ID
     */
    private String sessionId;
    
    /**
     * 用户IP地址
     */
    private String ipAddress;
    
    /**
     * 设备信息
     */
    private String deviceInfo;
    
    /**
     * 连接时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date connectTime;
    
    /**
     * 最后心跳时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastHeartbeatTime;
}
