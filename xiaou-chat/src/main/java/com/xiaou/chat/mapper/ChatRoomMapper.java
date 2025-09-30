package com.xiaou.chat.mapper;

import com.xiaou.chat.domain.ChatRoom;
import org.apache.ibatis.annotations.Mapper;

/**
 * 聊天室Mapper
 * 
 * @author xiaou
 */
@Mapper
public interface ChatRoomMapper {
    
    /**
     * 根据ID查询聊天室
     */
    ChatRoom selectById(Long id);
    
    /**
     * 查询官方聊天室
     */
    ChatRoom selectOfficialRoom();
}
