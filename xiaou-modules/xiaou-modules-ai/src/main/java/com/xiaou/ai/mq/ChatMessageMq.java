package com.xiaou.ai.mq;

import com.xiaou.ai.entity.ChatMessage;
import com.xiaou.ai.mapper.ChatMessageMapper;
import com.xiaou.mq.config.RabbitMQConfig;
import jakarta.annotation.Resource;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component

public class ChatMessageMq {

    @Resource
    private ChatMessageMapper baseMapper;

    @RabbitListener(queues = RabbitMQConfig.AICHAT_QUEUE)
    public void handleLogMessage(ChatMessage chatMessage) {
        baseMapper.insert(chatMessage);
    }

}
