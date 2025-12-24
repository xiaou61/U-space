package com.xiaou.chat.mapper;

import com.xiaou.chat.domain.ChatOnlineUser;
import com.xiaou.chat.dto.ChatOnlineUserResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 在线用户Mapper
 * 
 * @author xiaou
 */
@Mapper
public interface ChatOnlineUserMapper {
    
    /**
     * 插入在线用户
     */
    int insert(ChatOnlineUser user);
    
    /**
     * 根据会话ID查询
     */
    ChatOnlineUser selectBySessionId(String sessionId);
    
    /**
     * 根据会话ID删除
     */
    int deleteBySessionId(String sessionId);
    
    /**
     * 根据用户ID删除
     */
    int deleteByUserId(Long userId);
    
    /**
     * 更新心跳时间
     */
    int updateHeartbeat(String sessionId);
    
    /**
     * 统计在线人数
     */
    Integer countOnlineUsers(@Param("roomId") Long roomId);
    
    /**
     * 查询在线用户列表（带用户信息）
     */
    List<ChatOnlineUserResponse> selectOnlineUsers(@Param("roomId") Long roomId);
    
    /**
     * 删除超时的在线用户
     */
    int deleteTimeoutUsers(@Param("timeoutSeconds") Integer timeoutSeconds);
    
    /**
     * 根据用户ID查询用户信息（昵称、头像）
     */
    ChatOnlineUserResponse selectUserInfoByUserId(@Param("userId") Long userId);
}
