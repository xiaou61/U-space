package com.xiaou.chat.service;

import com.xiaou.chat.domain.ChatRoom;

/**
 * 聊天室Service接口
 * 
 * @author xiaou
 */
public interface ChatRoomService {
    
    /**
     * 获取官方聊天室
     */
    ChatRoom getOfficialRoom();
    
    /**
     * 根据ID查询聊天室
     */
    ChatRoom getById(Long id);
}
