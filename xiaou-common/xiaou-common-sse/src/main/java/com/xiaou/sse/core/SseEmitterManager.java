package com.xiaou.sse.core;

import cn.hutool.core.map.MapUtil;
import com.xiaou.common.constant.GlobalConstants;
import com.xiaou.redis.utils.RedisUtils;
import com.xiaou.sse.dto.SseMessageDto;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 管理 Server-Sent Events (SSE) 连接
 */
@Slf4j
@Component
public class SseEmitterManager {

    /**
     * 订阅的频道
     */
    private final static String SSE_TOPIC = "global:sse";

    private final static Map<String, Map<String, SseEmitter>> USER_TOKEN_EMITTERS = new ConcurrentHashMap<>();

    /**
     * 建立与指定用户的 SSE 连接
     *
     * @param userId 用户的唯一标识符，用于区分不同用户的连接
     * @param token  用户的唯一令牌，用于识别具体的连接
     * @return 返回一个 SseEmitter 实例，客户端可以通过该实例接收 SSE 事件
     */
    public SseEmitter connect(String userId, String token) {
        // 从 USER_TOKEN_EMITTERS 中获取或创建当前用户的 SseEmitter 映射表（ConcurrentHashMap）
        // 每个用户可以有多个 SSE 连接，通过 token 进行区分
        Map<String, SseEmitter> emitters = USER_TOKEN_EMITTERS.computeIfAbsent(userId, k -> new ConcurrentHashMap<>());

        // 创建一个新的 SseEmitter 实例，超时时间设置为 0 表示无限制
        SseEmitter emitter = new SseEmitter(0L);

        emitters.put(token, emitter);

        // 当 emitter 完成、超时或发生错误时，从映射表中移除对应的 token
        emitter.onCompletion(() -> emitters.remove(token));
        emitter.onTimeout(() -> emitters.remove(token));
        emitter.onError((e) -> emitters.remove(token));

        try {
            // 向客户端发送一条连接成功的事件
            emitter.send(SseEmitter.event().comment("connected"));
        } catch (IOException e) {
            // 如果发送消息失败，则从映射表中移除 emitter
            emitters.remove(token);
        }
        return emitter;
    }

    /**
     * 断开指定用户的 SSE 连接
     *
     * @param userId 用户的唯一标识符，用于区分不同用户的连接
     * @param token  用户的唯一令牌，用于识别具体的连接
     */
    public void disconnect(String  userId, String token) {
        if (userId == null || token == null) {
            return;
        }
        Map<String, SseEmitter> emitters = USER_TOKEN_EMITTERS.get(userId);
        if (MapUtil.isNotEmpty(emitters)) {
            try {
                SseEmitter sseEmitter = emitters.get(token);
                sseEmitter.send(SseEmitter.event().comment("disconnected"));
                sseEmitter.complete();
            } catch (Exception ignore) {
            }
            emitters.remove(token);
        } else {
            USER_TOKEN_EMITTERS.remove(userId);
        }
    }

    /**
     * 订阅SSE消息主题，并提供一个消费者函数来处理接收到的消息
     *
     * @param consumer 处理SSE消息的消费者函数
     */
    public void subscribeMessage(Consumer<SseMessageDto> consumer) {
        RedisUtils.subscribe(SSE_TOPIC, SseMessageDto.class, consumer);
    }

    /**
     * 向指定的用户会话发送消息
     *
     * @param userId  要发送消息的用户id
     * @param message 要发送的消息内容
     */
    public void sendMessage(String userId, String message,String type) {
        Map<String, SseEmitter> emitters = USER_TOKEN_EMITTERS.get(userId);
        if (MapUtil.isNotEmpty(emitters)) {
            for (Map.Entry<String, SseEmitter> entry : emitters.entrySet()) {
                try {
                    entry.getValue().send(SseEmitter.event()
                            .name(type)
                            .data(message));
                } catch (Exception e) {
                    emitters.remove(entry.getKey());
                }
            }
        } else {
            USER_TOKEN_EMITTERS.remove(userId);
        }
    }


    /**
     * 发布SSE订阅消息
     *
     * @param sseMessageDto 要发布的SSE消息对象
     */
    public void publishMessage(SseMessageDto sseMessageDto) {
        SseMessageDto broadcastMessage = new SseMessageDto();
        broadcastMessage.setMessage(sseMessageDto.getMessage());
        broadcastMessage.setUserIds(sseMessageDto.getUserIds());
        broadcastMessage.setType(sseMessageDto.getType());
        RedisUtils.publish(SSE_TOPIC, broadcastMessage, consumer -> {
            log.info("SSE发送主题订阅消息topic:{} session keys:{} message:{},type:{}",
                    SSE_TOPIC, sseMessageDto.getUserIds(), sseMessageDto.getMessage(), sseMessageDto.getType());
        });
    }

    /**
     * 向所有的用户发布订阅的消息(群发)
     */
    public void publishAll(SseMessageDto sseMessageDto) {
        SseMessageDto broadcastMessage = new SseMessageDto();
        broadcastMessage.setMessage(sseMessageDto.getMessage());
        broadcastMessage.setType(sseMessageDto.getType());


        RedisUtils.publish(SSE_TOPIC, broadcastMessage, consumer -> {
            log.info("SSE发送主题订阅消息topic:{} message:{} type:{}", SSE_TOPIC, sseMessageDto.getMessage(), sseMessageDto.getType());
        });
    }


    // 统计当前在线用户数量（有连接的用户数）
    public int getOnlineUserCount() {
        return USER_TOKEN_EMITTERS.size();
    }


    // 统计所有在线连接总数
    public int getTotalConnectionCount() {
        return USER_TOKEN_EMITTERS.values().stream()
                .mapToInt(Map::size)
                .sum();
    }

    // 统计某个用户的连接数
    public int getUserConnectionCount(String userId) {
        Map<String, SseEmitter> emitters = USER_TOKEN_EMITTERS.get(userId);
        return emitters == null ? 0 : emitters.size();
    }
    // 如果你想要所有在线用户列表
    public Set<String> getOnlineUserIds() {
        return USER_TOKEN_EMITTERS.keySet();
    }
    //查看指定用户是否在线
    public boolean isUserOnline(String userId) {
        return USER_TOKEN_EMITTERS.containsKey(userId);
    }


    public void sendMessageAll(String message, String type) {
        //要获取所有用户不管在线不在线
        List<String> userIds = RedisUtils.getCacheList(GlobalConstants.USER_ONLINE_KEY);
        for (String userId : userIds) {
            sendMessage(userId, message,type);
        }
    }
}
