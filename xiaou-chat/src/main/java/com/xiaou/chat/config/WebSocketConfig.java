package com.xiaou.chat.config;

import com.xiaou.chat.websocket.ChatWebSocketHandler;
import com.xiaou.chat.websocket.WebSocketInterceptor;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket配置类
 * 
 * @author xiaou
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Resource
    private ChatWebSocketHandler chatWebSocketHandler;
    @Resource
    private WebSocketInterceptor webSocketInterceptor;
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, "/ws/chat")
                .addInterceptors(webSocketInterceptor)
                .setAllowedOrigins("*");
    }
}
