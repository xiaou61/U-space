package com.xiaou.user.security;

import cn.hutool.core.util.StrUtil;
import com.xiaou.common.constant.Constants;
import com.xiaou.common.security.JwtTokenUtil;
import com.xiaou.common.security.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

/**
 * 用户端JWT认证过滤器
 *
 * @author xiaou
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserJwtAuthenticationFilter extends OncePerRequestFilter {

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
                if (jwtTokenUtil.validateToken(token)) {
                    // 检查用户类型
                    String userType = jwtTokenUtil.getUserTypeFromToken(token);
                    
                    // 只处理普通用户的token
                    if ("user".equals(userType)) {
                        // 验证Redis中的用户信息
                        String userInfoJson = tokenService.getUserFromToken(token, "user");
                        if (StrUtil.isNotBlank(userInfoJson)) {
                            String username = jwtTokenUtil.getUsernameFromToken(token);
                            
                            if (StrUtil.isNotBlank(username)) {
                                // 创建认证对象
                                UsernamePasswordAuthenticationToken authentication = 
                                    new UsernamePasswordAuthenticationToken(
                                        username, 
                                        null, 
                                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                                    );
                                
                                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                                
                                // 设置认证信息到Security上下文
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                                
                                log.debug("用户JWT认证成功: {}", username);
                            }
                        }
                    }
                } else {
                    log.debug("用户JWT Token验证失败");
                }
            }
        } catch (Exception e) {
            log.error("用户JWT认证过滤器异常", e);
        }
        
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // 只处理用户相关路径
        return !path.startsWith("/api/user/") && !path.startsWith("/api/captcha/");
    }
} 