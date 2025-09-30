package com.xiaou.chat.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.xiaou.chat.domain.ChatMessage;
import com.xiaou.chat.dto.*;
import com.xiaou.chat.mapper.ChatMessageMapper;
import com.xiaou.chat.service.ChatMessageService;
import com.xiaou.chat.service.ChatRoomService;
import com.xiaou.chat.service.ChatUserBanService;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.exception.BusinessException;
import com.xiaou.common.utils.PageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 聊天消息Service实现类
 * 
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {
    
    private final ChatMessageMapper chatMessageMapper;
    private final ChatRoomService chatRoomService;
    private final ChatUserBanService chatUserBanService;
    
    private static final int RECALL_TIME_LIMIT = 120; // 撤回时间限制：2分钟
    
    @Override
    public ChatMessage sendMessage(ChatMessageRequest request, Long userId, String ipAddress, String deviceInfo) {
        // 获取官方聊天室
        Long roomId = chatRoomService.getOfficialRoom().getId();
        
        // 检查用户是否被禁言
        if (chatUserBanService.isUserBanned(userId, roomId)) {
            throw new BusinessException("您已被禁言，无法发送消息");
        }
        
        // 创建消息记录
        ChatMessage message = new ChatMessage();
        message.setRoomId(roomId);
        message.setUserId(userId);
        message.setMessageType(request.getMessageType());
        message.setContent(request.getContent());
        message.setImageUrl(request.getImageUrl());
        message.setIsDeleted(0);
        message.setIpAddress(ipAddress);
        message.setDeviceInfo(deviceInfo);
        
        int result = chatMessageMapper.insert(message);
        if (result <= 0) {
            throw new BusinessException("发送消息失败");
        }
        
        log.info("用户发送消息，用户ID: {}, 消息ID: {}, 类型: {}", userId, message.getId(), request.getMessageType());
        
        // 查询完整消息信息（包含用户信息）
        return chatMessageMapper.selectById(message.getId());
    }
    
    @Override
    public ChatHistoryResponse getHistory(ChatHistoryRequest request) {
        Long roomId = chatRoomService.getOfficialRoom().getId();
        
        List<ChatMessage> messages = chatMessageMapper.selectHistory(
            roomId, 
            request.getLastMessageId(), 
            request.getPageSize()
        );
        
        // 反转列表，使旧消息在前
        Collections.reverse(messages);
        
        // 转换为响应DTO
        List<ChatMessageResponse> messageResponses = messages.stream()
            .map(this::convertToResponse)
            .collect(Collectors.toList());
        
        ChatHistoryResponse response = new ChatHistoryResponse();
        response.setMessages(messageResponses);
        response.setHasMore(messages.size() >= request.getPageSize());
        
        return response;
    }
    
    @Override
    public void recallMessage(Long messageId, Long userId) {
        ChatMessage message = chatMessageMapper.selectById(messageId);
        
        if (message == null) {
            throw new BusinessException("消息不存在");
        }
        
        if (!message.getUserId().equals(userId)) {
            throw new BusinessException("只能撤回自己的消息");
        }
        
        // 检查是否超过撤回时间限制
        long seconds = (System.currentTimeMillis() - message.getCreateTime().getTime()) / 1000;
        if (seconds > RECALL_TIME_LIMIT) {
            throw new BusinessException("消息发送超过2分钟，无法撤回");
        }
        
        int result = chatMessageMapper.deleteById(messageId);
        if (result <= 0) {
            throw new BusinessException("撤回消息失败");
        }
        
        log.info("用户撤回消息，用户ID: {}, 消息ID: {}", userId, messageId);
    }
    
    @Override
    public void deleteMessage(Long messageId) {
        int result = chatMessageMapper.deleteById(messageId);
        if (result <= 0) {
            throw new BusinessException("删除消息失败");
        }
        
        log.info("管理员删除消息，消息ID: {}", messageId);
    }
    
    @Override
    public void batchDeleteMessages(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的消息");
        }
        
        int result = chatMessageMapper.deleteBatch(ids);
        log.info("批量删除消息，数量: {}", result);
    }
    
    @Override
    public PageResult<ChatMessage> getAdminMessageList(AdminChatMessageListRequest request) {
        return PageHelper.doPage(request.getPageNum(), request.getPageSize(), () -> 
            chatMessageMapper.selectAdminMessageList(request)
        );
    }
    
    @Override
    public void sendAnnouncement(String content) {
        Long roomId = chatRoomService.getOfficialRoom().getId();
        
        ChatMessage message = new ChatMessage();
        message.setRoomId(roomId);
        message.setUserId(0L); // 系统消息用户ID为0
        message.setMessageType(3); // 类型3：系统消息
        message.setContent(content);
        message.setIsDeleted(0);
        
        int result = chatMessageMapper.insert(message);
        if (result <= 0) {
            throw new BusinessException("发送系统公告失败");
        }
        
        log.info("发送系统公告，消息ID: {}", message.getId());
    }
    
    /**
     * 转换为响应DTO
     */
    private ChatMessageResponse convertToResponse(ChatMessage message) {
        ChatMessageResponse response = BeanUtil.copyProperties(message, ChatMessageResponse.class);
        
        // 判断是否可撤回（2分钟内）
        long seconds = (System.currentTimeMillis() - message.getCreateTime().getTime()) / 1000;
        response.setCanRecall(seconds <= RECALL_TIME_LIMIT);
        
        return response;
    }
}
