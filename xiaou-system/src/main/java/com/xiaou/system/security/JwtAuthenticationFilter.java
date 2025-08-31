package com.xiaou.system.security;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.xiaou.common.constant.Constants;
import com.xiaou.common.security.JwtTokenUtil;
import com.xiaou.common.security.TokenService;
import com.xiaou.system.domain.SysAdmin;
import com.xiaou.system.service.SysAdminService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
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
    @Lazy
    private final SysAdminService sysAdminService;

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
                if (tokenService.validateToken(token, "admin")) {
                    // 检查用户类型，只处理管理员token
                    String userType = jwtTokenUtil.getUserTypeFromToken(token);
                    
                    if ("admin".equals(userType)) {
                        // 验证Redis中的管理员信息
                        String adminInfoJson = tokenService.getUserFromToken(token, "admin");
                        
                        // 如果需要重新加载管理员信息
                        if ("TOKEN_VALID_NEED_RELOAD".equals(adminInfoJson)) {
                            try {
                                Long adminId = jwtTokenUtil.getUserIdFromToken(token);
                                if (adminId != null) {
                                    SysAdmin admin = sysAdminService.getById(adminId);
                                    if (admin != null && admin.getStatus() == 0) { // 状态正常
                                        // 重新存储管理员信息到Redis
                                        adminInfoJson = JSONUtil.toJsonStr(admin);
                                        tokenService.storeUserInToken(token, adminInfoJson, "admin");
                                        log.info("管理员信息已重新加载到Redis: {}", admin.getUsername());
                                    } else {
                                        log.warn("管理员不存在或状态异常，拒绝认证: adminId={}", adminId);
                                        adminInfoJson = null;
                                    }
                                } else {
                                    log.warn("Token中无法获取管理员ID");
                                    adminInfoJson = null;
                                }
                            } catch (Exception e) {
                                log.error("重新加载管理员信息失败", e);
                                adminInfoJson = null;
                            }
                        }
                        
                        if (StrUtil.isNotBlank(adminInfoJson) && !"TOKEN_VALID_NEED_RELOAD".equals(adminInfoJson)) {
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
                                
                                log.debug("管理员JWT认证成功: {}", username);
                            }
                        }
                    }
                } else {
                    log.debug("管理员JWT Token验证失败");
                }
            }
        } catch (Exception e) {
            log.error("管理员JWT认证过滤器异常", e);
        }
        
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // 不处理用户相关路径（考虑context-path）
        return path.startsWith("/user/") || path.startsWith("/captcha/");
    }
} 