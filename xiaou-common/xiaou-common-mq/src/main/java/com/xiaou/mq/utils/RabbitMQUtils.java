package com.xiaou.mq.utils;

import com.xiaou.mq.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

@Component
public class RabbitMQUtils {

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void sendEmailMessage(Object message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.XIAOU_DIRECT_EXCHANGE, RabbitMQConfig.EMAIL_ROUTING_KEY, message);
    }

    public void sendNoticeMessage(Object message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.XIAOU_DIRECT_EXCHANGE, RabbitMQConfig.NOTICE_ROUTING_KEY, message);
    }

    public void sendLogMessage(Object message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.XIAOU_DIRECT_EXCHANGE, RabbitMQConfig.LOG_ROUTING_KEY, message);
    }

    public void sendAichatMessage(Object message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.XIAOU_DIRECT_EXCHANGE, RabbitMQConfig.AICHAT_ROUTING_KEY, message);
    }

    public void sendBbsMessage(Object message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.XIAOU_DIRECT_EXCHANGE, RabbitMQConfig.BBS_ROUTING_KEY, message);
    }

    /**
     * 发送抢课消息到队列
     */
    public void sendCourseGrabMessage(Object message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.XIAOU_DIRECT_EXCHANGE, RabbitMQConfig.COURSE_GRAB_ROUTING_KEY,
                message);
    }
}
