package com.xiaou.mq.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "xiaou.direct";
    public static final String QUEUE = "xiaou.email.queue";
    public static final String ROUTING_KEY = "xiaou.email.key";

    @Bean
    public DirectExchange userDirectExchange() {
        return new DirectExchange(EXCHANGE);
    }

    @Bean
    public Queue userQueue() {
        return new Queue(QUEUE);
    }

    @Bean
    public Binding userBinding(Queue userQueue, DirectExchange userDirectExchange) {
        return BindingBuilder.bind(userQueue).to(userDirectExchange).with(ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
