package com.xiaou.chat.service;

import com.xiaou.chat.domain.ChatOnlineUser;
import com.xiaou.chat.dto.ChatOnlineUserResponse;

import java.util.List;

/**
 * 在线用户Service接口
 * 
 * @author xiaou
 */
public interface ChatOnlineUserService {
    
    /**
     * 用户上线
     */
    void userOnline(Long userId, Long roomId, String sessionId, String ipAddress, String deviceInfo);
    
    /**
     * 用户下线
     */
    void userOffline(String sessionId);
    
    /**
     * 更新心跳
     */
    void updateHeartbeat(String sessionId);
    
    /**
     * 获取在线人数
     */
    Integer getOnlineCount(Long roomId);
    
    /**
     * 获取在线用户列表
     */
    List<ChatOnlineUserResponse> getOnlineUsers(Long roomId);
    
    /**
     * 踢出用户
     */
    void kickUser(Long userId);
    
    /**
     * 清理超时用户
     */
    void cleanTimeoutUsers();
}
