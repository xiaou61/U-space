package com.xiaou.chat.service;

import com.xiaou.chat.domain.ChatMessage;
import com.xiaou.chat.dto.*;
import com.xiaou.common.core.domain.PageResult;

import java.util.List;

/**
 * 聊天消息Service接口
 * 
 * @author xiaou
 */
public interface ChatMessageService {
    
    /**
     * 发送消息
     */
    ChatMessage sendMessage(ChatMessageRequest request, Long userId, String ipAddress, String deviceInfo);
    
    /**
     * 获取历史消息
     */
    ChatHistoryResponse getHistory(ChatHistoryRequest request);
    
    /**
     * 撤回消息
     */
    void recallMessage(Long messageId, Long userId);
    
    /**
     * 删除消息（管理员）
     */
    void deleteMessage(Long messageId);
    
    /**
     * 批量删除消息
     */
    void batchDeleteMessages(List<Long> ids);
    
    /**
     * 管理端查询消息列表
     */
    PageResult<ChatMessage> getAdminMessageList(AdminChatMessageListRequest request);
    
    /**
     * 发送系统公告
     */
    void sendAnnouncement(String content);
}
