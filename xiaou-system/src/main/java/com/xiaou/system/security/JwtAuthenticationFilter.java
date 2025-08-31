package com.xiaou.system.security;

import cn.hutool.core.util.StrUtil;
import com.xiaou.common.constant.Constants;
import com.xiaou.system.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT认证过滤器
 *
 * @author xiaou
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // 获取请求头中的Token
            String authHeader = request.getHeader(Constants.HEADER_AUTHORIZATION);
            String token = jwtTokenUtil.getTokenFromHeader(authHeader);
            
            // 如果Token存在且当前没有认证信息
            if (StrUtil.isNotBlank(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // 验证Token有效性
                if (tokenService.validateToken(token)) {
                    String username = tokenService.getUsernameFromToken(token);
                    
                    if (StrUtil.isNotBlank(username)) {
                        // 创建认证对象
                        UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(
                                username, 
                                null, 
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
                            );
                        
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        // 设置认证信息到Security上下文
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        
                        log.debug("JWT认证成功: {}", username);
                    }
                } else {
                    log.debug("JWT Token验证失败");
                }
            }
        } catch (Exception e) {
            log.error("JWT认证过滤器异常", e);
        }
        
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        log.debug("JWT过滤器检查路径: {} {}", method, path);
        
        // 跳过登录接口和其他公开接口
        boolean shouldSkip = path.equals("/api/auth/login") || 
               path.equals("/auth/login") ||
               path.startsWith("/swagger-ui") || 
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/druid") ||
               path.equals("/favicon.ico") ||
               path.equals("/error") ||
               "OPTIONS".equals(method);
               
        if (shouldSkip) {
            log.debug("跳过JWT过滤器: {} {}", method, path);
        }
        
        return shouldSkip;
    }
} 