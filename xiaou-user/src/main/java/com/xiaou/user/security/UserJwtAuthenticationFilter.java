package com.xiaou.user.security;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.xiaou.common.constant.Constants;
import com.xiaou.common.security.JwtTokenUtil;
import com.xiaou.common.security.TokenService;
import com.xiaou.user.domain.UserInfo;
import com.xiaou.user.service.UserInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.context.annotation.Lazy;

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
    @Lazy
    private final UserInfoService userInfoService;

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
                        
                        // 如果需要重新加载用户信息
                        if ("TOKEN_VALID_NEED_RELOAD".equals(userInfoJson)) {
                            try {
                                Long userId = jwtTokenUtil.getUserIdFromToken(token);
                                if (userId != null) {
                                    UserInfo userInfo = userInfoService.getUserById(userId);
                                    if (userInfo != null && userInfo.getStatus() == 0) { // 状态正常
                                        // 重新存储用户信息到Redis
                                        userInfoJson = JSONUtil.toJsonStr(userInfo);
                                        tokenService.storeUserInToken(token, userInfoJson, "user");
                                        log.info("用户信息已重新加载到Redis: {}", userInfo.getUsername());
                                    } else {
                                        log.warn("用户不存在或状态异常，拒绝认证: userId={}", userId);
                                        userInfoJson = null;
                                    }
                                } else {
                                    log.warn("Token中无法获取用户ID");
                                    userInfoJson = null;
                                }
                            } catch (Exception e) {
                                log.error("重新加载用户信息失败", e);
                                userInfoJson = null;
                            }
                        }
                        
                        if (StrUtil.isNotBlank(userInfoJson) && !"TOKEN_VALID_NEED_RELOAD".equals(userInfoJson)) {
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
        // 只处理用户相关路径（考虑context-path）
        return !path.startsWith("/user/") && !path.startsWith("/captcha/");
    }
} 