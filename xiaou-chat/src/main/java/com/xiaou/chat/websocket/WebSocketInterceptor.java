package com.xiaou.chat.websocket;

import com.xiaou.common.security.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket握手拦截器 - 用于JWT认证
 * 
 * @author xiaou
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketInterceptor implements HandshakeInterceptor {
    
    private final JwtTokenUtil jwtTokenUtil;
    
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        try {
            if (request instanceof ServletServerHttpRequest) {
                ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
                
                // 从URL参数中获取token
                String query = servletRequest.getURI().getQuery();
                if (query != null && query.contains("token=")) {
                    String token = query.split("token=")[1].split("&")[0];
                    
                    // 验证token
                    if (jwtTokenUtil.validateToken(token)) {
                        Long userId = jwtTokenUtil.getUserIdFromToken(token);
                        String username = jwtTokenUtil.getUsernameFromToken(token);
                        
                        // 将用户信息存入attributes，后续可以使用
                        attributes.put("userId", userId);
                        attributes.put("username", username);
                        attributes.put("token", token);
                        
                        log.info("WebSocket握手成功，用户ID: {}, 用户名: {}", userId, username);
                        return true;
                    } else {
                        log.warn("WebSocket握手失败，Token无效");
                    }
                } else {
                    log.warn("WebSocket握手失败，未提供Token");
                }
            }
        } catch (Exception e) {
            log.error("WebSocket握手异常", e);
        }
        
        return false;
    }
    
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                              WebSocketHandler wsHandler, Exception exception) {
        // 握手后处理
    }
}
