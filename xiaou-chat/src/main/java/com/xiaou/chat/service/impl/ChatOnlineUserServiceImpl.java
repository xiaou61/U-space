package com.xiaou.chat.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.xiaou.chat.domain.ChatOnlineUser;
import com.xiaou.chat.dto.ChatOnlineUserResponse;
import com.xiaou.chat.mapper.ChatOnlineUserMapper;
import com.xiaou.chat.service.ChatOnlineUserService;
import com.xiaou.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 在线用户Service实现类
 * 
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatOnlineUserServiceImpl implements ChatOnlineUserService {
    
    private final ChatOnlineUserMapper chatOnlineUserMapper;
    
    private static final int TIMEOUT_SECONDS = 60; // 超时时间60秒
    
    @Override
    public void userOnline(Long userId, Long roomId, String sessionId, String ipAddress, String deviceInfo) {
        ChatOnlineUser user = new ChatOnlineUser();
        user.setUserId(userId);
        user.setRoomId(roomId);
        user.setSessionId(sessionId);
        user.setIpAddress(ipAddress);
        user.setDeviceInfo(deviceInfo);
        user.setConnectTime(new Date());
        user.setLastHeartbeatTime(new Date());
        
        int result = chatOnlineUserMapper.insert(user);
        if (result <= 0) {
            throw new BusinessException("用户上线记录失败");
        }
        
        log.info("用户上线，用户ID: {}, 会话ID: {}", userId, sessionId);
    }
    
    @Override
    public void userOffline(String sessionId) {
        int result = chatOnlineUserMapper.deleteBySessionId(sessionId);
        if (result > 0) {
            log.info("用户下线，会话ID: {}", sessionId);
        }
    }
    
    @Override
    public void updateHeartbeat(String sessionId) {
        chatOnlineUserMapper.updateHeartbeat(sessionId);
    }
    
    @Override
    public Integer getOnlineCount(Long roomId) {
        return chatOnlineUserMapper.countOnlineUsers(roomId);
    }
    
    @Override
    public List<ChatOnlineUserResponse> getOnlineUsers(Long roomId) {
        return chatOnlineUserMapper.selectOnlineUsers(roomId);
    }
    
    @Override
    public void kickUser(Long userId) {
        int result = chatOnlineUserMapper.deleteByUserId(userId);
        if (result > 0) {
            log.info("踢出用户，用户ID: {}", userId);
        }
    }
    
    @Override
    public void cleanTimeoutUsers() {
        int result = chatOnlineUserMapper.deleteTimeoutUsers(TIMEOUT_SECONDS);
        if (result > 0) {
            log.info("清理超时用户，数量: {}", result);
        }
    }
}
