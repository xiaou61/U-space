package com.xiaou.chat.websocket;

import cn.dev33.satoken.stp.StpUtil;
import com.xiaou.common.satoken.StpUserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 握手拦截器 - 基于 Sa-Token 认证
 * 
 * @author xiaou
 */
@Slf4j
@Component
public class SaTokenWebSocketInterceptor implements HandshakeInterceptor {
    
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
                    
                    // 使用 Sa-Token 验证 Token
                    try {
                        // 尝试从 Token 获取登录 ID（Sa-Token 方式）
                        Object loginId = StpUserUtil.stpLogic.getLoginIdByToken(token);
                        
                        if (loginId != null) {
                            Long userId = Long.parseLong(loginId.toString());
                            
                            // 将用户信息存入attributes，后续可以使用
                            attributes.put("userId", userId);
                            attributes.put("token", token);
                            
                            log.info("WebSocket握手成功（Sa-Token），用户ID: {}", userId);
                            return true;
                        } else {
                            log.warn("WebSocket握手失败，Token无效（Sa-Token）");
                        }
                    } catch (Exception e) {
                        log.warn("WebSocket握手失败，Token验证异常: {}", e.getMessage());
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
        if (exception != null) {
            log.error("WebSocket握手后处理异常", exception);
        }
    }
}
