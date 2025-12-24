package com.xiaou.chat.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.xiaou.chat.domain.ChatOnlineUser;
import com.xiaou.chat.dto.ChatOnlineUserResponse;
import com.xiaou.chat.mapper.ChatOnlineUserMapper;
import com.xiaou.chat.service.ChatOnlineUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 在线用户Service实现类 - 使用Redis缓存优化
 * 
 * <p>优化策略：</p>
 * <ul>
 *   <li>在线用户信息存储在Redis，减少数据库查询</li>
 *   <li>心跳更新只更新Redis，不操作数据库</li>
 *   <li>用户上下线时同步更新Redis和数据库</li>
 *   <li>定时清理过期用户从 Redis 中删除</li>
 * </ul>
 * 
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatOnlineUserServiceImpl implements ChatOnlineUserService {
    
    private final ChatOnlineUserMapper chatOnlineUserMapper;
    private final RedissonClient redissonClient;
    
    private static final int TIMEOUT_SECONDS = 90; // 超时时间90秒
    private static final String ONLINE_USERS_KEY = "chat:online:users";  // Hash: sessionId -> userInfo JSON
    private static final String HEARTBEAT_KEY = "chat:online:heartbeat"; // Hash: sessionId -> timestamp
    private static final String ROOM_USERS_KEY = "chat:room:%d:users";   // Set: roomId -> sessionIds
    
    @Override
    public void userOnline(Long userId, Long roomId, String sessionId, String ipAddress, String deviceInfo) {
        Date now = new Date();
        
        // 0. 先清理该用户在该房间的旧会话（防止重复）
        cleanUserOldSessions(userId, roomId);
        
        // 1. 存入Redis
        ChatOnlineUserResponse userInfo = new ChatOnlineUserResponse();
        userInfo.setUserId(userId);
        userInfo.setSessionId(sessionId);
        userInfo.setRoomId(roomId);
        userInfo.setIpAddress(ipAddress);
        userInfo.setDeviceInfo(deviceInfo);
        userInfo.setConnectTime(now);
        
        // 获取用户昵称和头像（从数据库查询一次）
        try {
            ChatOnlineUserResponse existUser = chatOnlineUserMapper.selectUserInfoByUserId(userId);
            if (existUser != null) {
                userInfo.setUserNickname(existUser.getUserNickname());
                userInfo.setUserAvatar(existUser.getUserAvatar());
            }
        } catch (Exception e) {
            log.warn("获取用户信息失败: {}", e.getMessage());
        }
        
        // 存储用户信息到Redis Hash
        RMap<String, String> usersMap = redissonClient.getMap(ONLINE_USERS_KEY);
        usersMap.put(sessionId, JSONUtil.toJsonStr(userInfo));
        
        // 存储心跳时间
        RMap<String, Long> heartbeatMap = redissonClient.getMap(HEARTBEAT_KEY);
        heartbeatMap.put(sessionId, System.currentTimeMillis());
        
        // 添加到房间用户集合
        RSet<String> roomUsers = redissonClient.getSet(String.format(ROOM_USERS_KEY, roomId));
        roomUsers.add(sessionId);
        
        // 2. 异步存入数据库（用于持久化，可选）
        try {
            ChatOnlineUser user = new ChatOnlineUser();
            user.setUserId(userId);
            user.setRoomId(roomId);
            user.setSessionId(sessionId);
            user.setIpAddress(ipAddress);
            user.setDeviceInfo(deviceInfo);
            user.setConnectTime(now);
            user.setLastHeartbeatTime(now);
            chatOnlineUserMapper.insert(user);
        } catch (Exception e) {
            log.warn("用户上线记录到数据库失败: {}", e.getMessage());
        }
        
        log.info("用户上线，用户ID: {}, 会话ID: {}", userId, sessionId);
    }
    
    @Override
    public void userOffline(String sessionId) {
        // 1. 从Redis删除
        RMap<String, String> usersMap = redissonClient.getMap(ONLINE_USERS_KEY);
        String userInfoJson = usersMap.remove(sessionId);
        
        RMap<String, Long> heartbeatMap = redissonClient.getMap(HEARTBEAT_KEY);
        heartbeatMap.remove(sessionId);
        
        // 从房间用户集合删除
        if (StrUtil.isNotBlank(userInfoJson)) {
            try {
                ChatOnlineUserResponse userInfo = JSONUtil.toBean(userInfoJson, ChatOnlineUserResponse.class);
                if (userInfo.getRoomId() != null) {
                    RSet<String> roomUsers = redissonClient.getSet(String.format(ROOM_USERS_KEY, userInfo.getRoomId()));
                    roomUsers.remove(sessionId);
                }
            } catch (Exception e) {
                log.warn("解析用户信息失败: {}", e.getMessage());
            }
        }
        
        // 2. 从数据库删除
        try {
            chatOnlineUserMapper.deleteBySessionId(sessionId);
        } catch (Exception e) {
            log.warn("从数据库删除用户失败: {}", e.getMessage());
        }
        
        log.info("用户下线，会话ID: {}", sessionId);
    }
    
    @Override
    public void updateHeartbeat(String sessionId) {
        // 只更新Redis，不操作数据库！
        RMap<String, Long> heartbeatMap = redissonClient.getMap(HEARTBEAT_KEY);
        heartbeatMap.put(sessionId, System.currentTimeMillis());
    }
    
    @Override
    public Integer getOnlineCount(Long roomId) {
        // 从Redis获取在线人数
        RSet<String> roomUsers = redissonClient.getSet(String.format(ROOM_USERS_KEY, roomId));
        return roomUsers.size();
    }
    
    @Override
    public List<ChatOnlineUserResponse> getOnlineUsers(Long roomId) {
        // 从Redis获取在线用户列表
        RSet<String> roomUsers = redissonClient.getSet(String.format(ROOM_USERS_KEY, roomId));
        RMap<String, String> usersMap = redissonClient.getMap(ONLINE_USERS_KEY);
        
        List<ChatOnlineUserResponse> result = new ArrayList<>();
        List<String> orphanSessions = new ArrayList<>();
        
        for (String sessionId : roomUsers) {
            String userInfoJson = usersMap.get(sessionId);
            if (StrUtil.isNotBlank(userInfoJson)) {
                try {
                    ChatOnlineUserResponse userInfo = JSONUtil.toBean(userInfoJson, ChatOnlineUserResponse.class);
                    result.add(userInfo);
                } catch (Exception e) {
                    log.warn("解析用户信息失败: {}", e.getMessage());
                    orphanSessions.add(sessionId);
                }
            } else {
                // 孤儿 sessionId，标记清理
                orphanSessions.add(sessionId);
            }
        }
        
        // 清理孤儿数据
        for (String sid : orphanSessions) {
            roomUsers.remove(sid);
        }
        
        return result;
    }
    
    @Override
    public void kickUser(Long userId) {
        // 从Redis查找该用户的所有会话并删除
        RMap<String, String> usersMap = redissonClient.getMap(ONLINE_USERS_KEY);
        List<String> sessionsToRemove = new ArrayList<>();
        
        for (Map.Entry<String, String> entry : usersMap.entrySet()) {
            try {
                ChatOnlineUserResponse userInfo = JSONUtil.toBean(entry.getValue(), ChatOnlineUserResponse.class);
                if (userId.equals(userInfo.getUserId())) {
                    sessionsToRemove.add(entry.getKey());
                }
            } catch (Exception e) {
                log.warn("解析用户信息失败: {}", e.getMessage());
            }
        }
        
        for (String sessionId : sessionsToRemove) {
            userOffline(sessionId);
        }
        
        // 从数据库删除
        try {
            chatOnlineUserMapper.deleteByUserId(userId);
        } catch (Exception e) {
            log.warn("从数据库踢出用户失败: {}", e.getMessage());
        }
        
        log.info("踢出用户，用户ID: {}", userId);
    }
    
    /**
     * 清理用户在指定房间的旧会话
     */
    private void cleanUserOldSessions(Long userId, Long roomId) {
        RSet<String> roomUsers = redissonClient.getSet(String.format(ROOM_USERS_KEY, roomId));
        RMap<String, String> usersMap = redissonClient.getMap(ONLINE_USERS_KEY);
        RMap<String, Long> heartbeatMap = redissonClient.getMap(HEARTBEAT_KEY);
        
        List<String> sessionsToRemove = new ArrayList<>();
        for (String sid : roomUsers) {
            String userInfoJson = usersMap.get(sid);
            if (StrUtil.isBlank(userInfoJson)) {
                // 孤儿 sessionId，没有对应用户数据，直接清理
                sessionsToRemove.add(sid);
            } else {
                try {
                    ChatOnlineUserResponse info = JSONUtil.toBean(userInfoJson, ChatOnlineUserResponse.class);
                    if (userId.equals(info.getUserId())) {
                        sessionsToRemove.add(sid);
                    }
                } catch (Exception e) {
                    log.warn("解析用户信息失败: {}", e.getMessage());
                    sessionsToRemove.add(sid); // 解析失败也清理
                }
            }
        }
        
        for (String sid : sessionsToRemove) {
            usersMap.remove(sid);
            heartbeatMap.remove(sid);
            roomUsers.remove(sid);
        }
        
        // 清理数据库中的旧记录
        if (!sessionsToRemove.isEmpty()) {
            try {
                chatOnlineUserMapper.deleteByUserId(userId);
            } catch (Exception e) {
                log.warn("清理用户旧会话失败: {}", e.getMessage());
            }
        }
    }
    
    @Override
    public void cleanTimeoutUsers() {
        // 清理Redis中超时的用户
        RMap<String, Long> heartbeatMap = redissonClient.getMap(HEARTBEAT_KEY);
        long now = System.currentTimeMillis();
        long timeoutThreshold = now - (TIMEOUT_SECONDS * 1000L);
        
        List<String> timeoutSessions = new ArrayList<>();
        for (Map.Entry<String, Long> entry : heartbeatMap.entrySet()) {
            if (entry.getValue() < timeoutThreshold) {
                timeoutSessions.add(entry.getKey());
            }
        }
        
        for (String sessionId : timeoutSessions) {
            userOffline(sessionId);
        }
        
        // 也清理数据库中的超时用户
        try {
            int result = chatOnlineUserMapper.deleteTimeoutUsers(TIMEOUT_SECONDS);
            if (result > 0 || !timeoutSessions.isEmpty()) {
                log.info("清理超时用户，Redis: {}, DB: {}", timeoutSessions.size(), result);
            }
        } catch (Exception e) {
            log.warn("清理数据库超时用户失败: {}", e.getMessage());
        }
    }
    
}
