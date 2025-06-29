package com.xiaou.mq.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MessageListener {

    @RabbitListener(queues = "xiaou.user.queue")
    public void handleMessage(String message) {
        log.info("接收到消息: {}", message);
    }
}
