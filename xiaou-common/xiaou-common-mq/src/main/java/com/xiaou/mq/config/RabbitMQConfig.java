package com.xiaou.mq.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 统一配置类，包含 Email 通知队列 和 公告通知队列绑定
 */
@Configuration
public class RabbitMQConfig {

    // === 公共交换机 ===
    public static final String XIAOU_DIRECT_EXCHANGE = "xiaou.direct";

    // === Email 通知相关配置 ===
    public static final String EMAIL_QUEUE = "xiaou.email.queue";
    public static final String EMAIL_ROUTING_KEY = "xiaou.email.key";

    // === 公告通知相关配置 ===
    public static final String NOTICE_QUEUE = "xiaou.notice.queue";
    public static final String NOTICE_ROUTING_KEY = "xiaou.notice.key";

    // === 日志配置 ===
    public static final String LOG_QUEUE = "xiaou.log.queue";
    public static final String LOG_ROUTING_KEY = "xiaou.log.key";

    // === AI对话存储配置 ===
    public static final String AICHAT_QUEUE = "xiaou.ai.chat.queue";
    public static final String AICHAT_ROUTING_KEY = "xiaou.ai.chat.key";
    // === BBS论坛配置 ===
    public static final String BBS_QUEUE = "xiaou.bbs.queue";
    public static final String BBS_ROUTING_KEY = "xiaou.bbs.key";
    // ======== 基础配置 ========

    /**
     * 声明直连交换机
     */
    @Bean
    public DirectExchange directExchange() {
        return new DirectExchange(XIAOU_DIRECT_EXCHANGE);
    }

    // ======== Email 配置 ========

    @Bean
    public Queue emailQueue() {
        return new Queue(EMAIL_QUEUE);
    }

    @Bean
    public Binding emailBinding(Queue emailQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(emailQueue)
                .to(directExchange)
                .with(EMAIL_ROUTING_KEY);
    }

    // ======== Notice 配置 ========

    @Bean
    public Queue noticeQueue() {
        return new Queue(NOTICE_QUEUE);
    }

    @Bean
    public Binding noticeBinding(Queue noticeQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(noticeQueue)
                .to(directExchange)
                .with(NOTICE_ROUTING_KEY);
    }

    // ======== Log 配置 ========
    @Bean
    public Queue logQueue() {
        return new Queue(LOG_QUEUE);
    }

    @Bean
    public Binding logBinding(Queue logQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(logQueue)
                .to(directExchange)
                .with(LOG_ROUTING_KEY);
    }
    // =========AIChat对话信息存储 配置=========
    @Bean
    public Queue aiChatQueue() {
        return new Queue(AICHAT_QUEUE);
    }
    @Bean
    public Binding aiChatBinding(Queue aiChatQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(aiChatQueue)
                .to(directExchange)
                .with(AICHAT_ROUTING_KEY);
    }
    // === BBS论坛配置 ===
    @Bean
    public Queue bbsQueue() {
        return new Queue(BBS_QUEUE);
    }
    @Bean
    public Binding bbsBinding(Queue bbsQueue, DirectExchange directExchange) {
        return BindingBuilder.bind(bbsQueue)
                .to(directExchange)
                .with(BBS_ROUTING_KEY);
    }


    // ======== 消息序列化配置 ========

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
