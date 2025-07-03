package com.xiaou.sse.listener;

import cn.hutool.core.collection.CollUtil;
import com.xiaou.mq.utils.RabbitMQUtils;
import com.xiaou.sse.core.SseEmitterManager;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;

/**
 * SSE 主题订阅监听器
 */
@Slf4j
public class SseTopicListener implements ApplicationRunner, Ordered {

    @Autowired
    private SseEmitterManager sseEmitterManager;
    @Resource
    private RabbitMQUtils rabbitMQUtils;

    /**
     * 在Spring Boot应用程序启动时初始化SSE主题订阅监听器
     *
     * @param args 应用程序参数
     * @throws Exception 初始化过程中可能抛出的异常
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        sseEmitterManager.subscribeMessage((message) -> {

            log.info("SSE主题订阅收到消息session keys={} message={} type={}", message.getUserIds(), message.getMessage(), message.getType());
            // 如果key不为空就按照key发消息 如果为空就群发
            if (CollUtil.isNotEmpty(message.getUserIds())) {
                message.getUserIds().forEach(key -> {
                    sseEmitterManager.sendMessage(key, message.getMessage(),message.getType());
                    //调用mq进行存储
                    rabbitMQUtils.sendNoticeMessage(message);
                });
            } else {
                //群发所有用户
                sseEmitterManager.sendMessageAll(message.getMessage(),message.getType());
                //调用mq进行存储
                log.info("SSE主题订阅群发消息 message={} type={}", message.getMessage(), message.getType());
                rabbitMQUtils.sendNoticeMessage(message);
            }
        });
        log.info("初始化SSE主题订阅监听器成功");
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
