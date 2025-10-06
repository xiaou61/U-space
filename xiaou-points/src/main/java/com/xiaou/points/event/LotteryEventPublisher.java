package com.xiaou.points.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 抽奖事件发布器
 * 
 * @author xiaou
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LotteryEventPublisher {
    
    private final List<LotteryEventListener> listeners;
    
    /**
     * 发布事件（异步）
     */
    public void publish(LotteryEvent event) {
        log.info("发布抽奖事件: {}", event.getType());
        
        for (LotteryEventListener listener : listeners) {
            try {
                // 异步执行监听器
                new Thread(() -> {
                    try {
                        listener.onEvent(event);
                    } catch (Exception e) {
                        log.error("监听器{}处理事件失败", listener.getListenerName(), e);
                    }
                }).start();
            } catch (Exception e) {
                log.error("发布事件到监听器{}失败", listener.getListenerName(), e);
            }
        }
    }
    
    /**
     * 发布事件（同步）
     */
    public void publishSync(LotteryEvent event) {
        log.info("同步发布抽奖事件: {}", event.getType());
        
        for (LotteryEventListener listener : listeners) {
            try {
                listener.onEvent(event);
            } catch (Exception e) {
                log.error("监听器{}处理事件失败", listener.getListenerName(), e);
            }
        }
    }
}

