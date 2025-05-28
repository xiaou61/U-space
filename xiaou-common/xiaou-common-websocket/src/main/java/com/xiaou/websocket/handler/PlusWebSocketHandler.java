package com.xiaou.websocket.handler;

import cn.hutool.core.util.ObjectUtil;
import com.xiaou.utils.LoginHelper;
import com.xiaou.websocket.dto.WebSocketMessageDto;
import com.xiaou.websocket.holder.WebSocketSessionHolder;
import com.xiaou.websocket.utils.WebSocketUtils;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.util.List;

import static com.xiaou.websocket.constant.WebSocketConstants.LOGIN_USER_KEY;


/**
 * WebSocketHandler 实现类
 */
@Slf4j
public class PlusWebSocketHandler extends AbstractWebSocketHandler {

    /**
     * 连接成功后
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        Long userId = Long.valueOf(session.getAttributes().get(LOGIN_USER_KEY).toString());
        if (ObjectUtil.isNull(userId)) {
            session.close(CloseStatus.BAD_DATA);
            log.info("[connect] invalid token received. sessionId: {}", session.getId());
            return;
        }
        WebSocketSessionHolder.addSession(userId, session);
        log.info("[connect] sessionId: {},userId:{}", session.getId(), userId);
    }

    /**
     * 处理接收到的文本消息
     *
     * @param session WebSocket会话
     * @param message 接收到的文本消息
     * @throws Exception 处理消息过程中可能抛出的异常
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 从WebSocket会话中获取登录用户信息
        Long userId = Long.valueOf(session.getAttributes().get(LOGIN_USER_KEY).toString());

        // 创建WebSocket消息DTO对象
        WebSocketMessageDto webSocketMessageDto = new WebSocketMessageDto();
        webSocketMessageDto.setSessionKeys(List.of(userId));
        webSocketMessageDto.setMessage(message.getPayload());
        WebSocketUtils.publishMessage(webSocketMessageDto);
    }

    /**
     * 处理接收到的二进制消息
     *
     * @param session WebSocket会话
     * @param message 接收到的二进制消息
     * @throws Exception 处理消息过程中可能抛出的异常
     */
    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        super.handleBinaryMessage(session, message);
    }

    /**
     * 处理接收到的Pong消息（心跳监测）
     *
     * @param session WebSocket会话
     * @param message 接收到的Pong消息
     * @throws Exception 处理消息过程中可能抛出的异常
     */
    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        WebSocketUtils.sendPongMessage(session);
    }

    /**
     * 处理WebSocket传输错误
     *
     * @param session   WebSocket会话
     * @param exception 发生的异常
     * @throws Exception 处理过程中可能抛出的异常
     */
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("[transport error] sessionId: {} , exception:{}", session.getId(), exception.getMessage());
    }

    /**
     * 在WebSocket连接关闭后执行清理操作
     *
     * @param session WebSocket会话
     * @param status  关闭状态信息
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = Long.valueOf(session.getAttributes().get(LOGIN_USER_KEY).toString());
        if (ObjectUtil.isNull(userId)) {
            log.info("[disconnect] invalid token received. sessionId: {}", session.getId());
            return;
        }
        WebSocketSessionHolder.removeSession(userId);
        log.info("[disconnect] sessionId: {},userId:{}", session.getId(), userId);
    }

    /**
     * 指示处理程序是否支持接收部分消息
     *
     * @return 如果支持接收部分消息，则返回true；否则返回false
     */
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

}
