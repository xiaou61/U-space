package com.xiaou.chat.mapper;

import com.xiaou.chat.domain.ChatMessage;
import com.xiaou.chat.dto.AdminChatMessageListRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 聊天消息Mapper
 * 
 * @author xiaou
 */
@Mapper
public interface ChatMessageMapper {
    
    /**
     * 插入消息
     */
    int insert(ChatMessage message);
    
    /**
     * 根据ID查询消息
     */
    ChatMessage selectById(Long id);
    
    /**
     * 查询历史消息（倒序）
     */
    List<ChatMessage> selectHistory(@Param("roomId") Long roomId, 
                                    @Param("lastMessageId") Long lastMessageId, 
                                    @Param("pageSize") Integer pageSize);
    
    /**
     * 删除消息（软删除）
     */
    int deleteById(Long id);
    
    /**
     * 批量删除消息
     */
    int deleteBatch(@Param("ids") List<Long> ids);
    
    /**
     * 管理端查询消息总数
     */
    Long selectAdminMessageCount(@Param("request") AdminChatMessageListRequest request);
    
    /**
     * 管理端查询消息列表
     */
    List<ChatMessage> selectAdminMessageList(@Param("request") AdminChatMessageListRequest request);
}
