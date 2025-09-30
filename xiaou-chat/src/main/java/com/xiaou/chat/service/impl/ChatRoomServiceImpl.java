package com.xiaou.chat.service.impl;

import com.xiaou.chat.domain.ChatRoom;
import com.xiaou.chat.mapper.ChatRoomMapper;
import com.xiaou.chat.service.ChatRoomService;
import com.xiaou.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 聊天室Service实现类
 * 
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService {
    
    private final ChatRoomMapper chatRoomMapper;
    
    @Override
    public ChatRoom getOfficialRoom() {
        ChatRoom room = chatRoomMapper.selectOfficialRoom();
        if (room == null) {
            throw new BusinessException("官方聊天室不存在");
        }
        return room;
    }
    
    @Override
    public ChatRoom getById(Long id) {
        ChatRoom room = chatRoomMapper.selectById(id);
        if (room == null) {
            throw new BusinessException("聊天室不存在");
        }
        return room;
    }
}
