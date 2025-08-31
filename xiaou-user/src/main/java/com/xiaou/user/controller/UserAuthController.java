package com.xiaou.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaou.common.core.domain.Result;
import com.xiaou.common.security.JwtTokenUtil;
import com.xiaou.common.security.TokenService;
import com.xiaou.common.utils.UserContextUtil;
import com.xiaou.user.domain.UserInfo;
import com.xiaou.user.dto.UserInfoResponse;
import com.xiaou.user.dto.UserLoginRequest;
import com.xiaou.user.dto.UserLoginResponse;
import com.xiaou.user.dto.UserRegisterRequest;
import com.xiaou.user.service.UserInfoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户认证控制器
 *
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/user/auth")
@RequiredArgsConstructor
public class UserAuthController {

    private final UserInfoService userInfoService;
    private final TokenService tokenService;
    private final JwtTokenUtil jwtTokenUtil;
    private final ObjectMapper objectMapper;
    private final UserContextUtil userContextUtil;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result<UserInfoResponse> register(@Valid @RequestBody UserRegisterRequest request) {
        try {
            log.info("用户注册，用户名: {}", request.getUsername());
            
            UserInfo user = userInfoService.register(request);
            UserInfoResponse userResponse = userInfoService.getUserInfoById(user.getId());
            
            log.info("用户注册成功，用户ID: {}", user.getId());
            return Result.success("注册成功", userResponse);
            
        } catch (Exception e) {
            log.error("用户注册失败，用户名: {}", request.getUsername(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest request) {
        try {
            log.info("用户登录，用户名: {}", request.getUsername());
            
            UserLoginResponse response = userInfoService.login(request, String.valueOf(123L));
            
            log.info("用户登录成功，用户名: {}", request.getUsername());
            return Result.success("登录成功", response);
            
        } catch (Exception e) {
            log.error("用户登录失败，用户名: {}", request.getUsername(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = jwtTokenUtil.getTokenFromHeader(authHeader);
            if (token != null) {
                // 将Token加入黑名单
                tokenService.addToBlacklist(token, "user");
                // 删除Redis中的用户信息
                tokenService.deleteToken(token, "user");
                
                String username = tokenService.getUsernameFromToken(token);
                log.info("用户登出成功，用户: {}", username);
            }
            
            return Result.success("登出成功", null);
            
        } catch (Exception e) {
            log.error("用户登出失败", e);
            return Result.error("登出失败");
        }
    }

    /**
     * 刷新Token
     */
    @PostMapping("/refresh")
    public Result<Map<String, Object>> refresh(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = jwtTokenUtil.getTokenFromHeader(authHeader);
            if (token == null) {
                return Result.error("Token无效");
            }
            
            String newToken = tokenService.refreshToken(token, "user");
            if (newToken == null) {
                return Result.error("Token已过期，请重新登录");
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("accessToken", newToken);
            result.put("tokenType", "Bearer");
            result.put("expiresIn", jwtTokenUtil.getTokenRemainingTime(newToken));
            
            log.info("Token刷新成功");
            return Result.success("Token刷新成功", result);
            
        } catch (Exception e) {
            log.error("Token刷新失败", e);
            return Result.error("Token刷新失败");
        }
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<UserInfoResponse> getUserInfo() {
        try {
            // 使用 UserContextUtil 获取当前用户信息
            UserContextUtil.UserInfo currentUser = userContextUtil.getCurrentUser();
            if (currentUser == null) {
                return Result.error("Token无效或已过期");
            }
            
            // 验证是否为普通用户
            if (!currentUser.isUser()) {
                return Result.error("权限不足");
            }
            
            UserInfoResponse userInfo = userInfoService.getUserInfoById(currentUser.getId());
            
            log.debug("获取用户信息成功: {}", currentUser.getUsername());
            return Result.success("获取成功", userInfo);
            
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return Result.error("获取用户信息失败");
        }
    }

    /**
     * 检查用户名是否可用
     */
    @GetMapping("/check-username")
    public Result<Map<String, Boolean>> checkUsername(@RequestParam String username) {
        try {
            log.info("检查用户名可用性，用户名: {}", username);
            
            boolean available = userInfoService.isUsernameAvailable(username);
            
            Map<String, Boolean> result = new HashMap<>();
            result.put("available", available);
            
            return Result.success("检查完成", result);
            
        } catch (Exception e) {
            log.error("检查用户名可用性失败，用户名: {}", username, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 检查邮箱是否可用
     */
    @GetMapping("/check-email")
    public Result<Map<String, Boolean>> checkEmail(@RequestParam String email) {
        try {
            log.info("检查邮箱可用性，邮箱: {}", email);
            
            boolean available = userInfoService.isEmailAvailable(email);
            
            Map<String, Boolean> result = new HashMap<>();
            result.put("available", available);
            
            return Result.success("检查完成", result);
            
        } catch (Exception e) {
            log.error("检查邮箱可用性失败，邮箱: {}", email, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 检查手机号是否可用
     */
    @GetMapping("/check-phone")
    public Result<Map<String, Boolean>> checkPhone(@RequestParam String phone) {
        try {
            log.info("检查手机号可用性，手机号: {}", phone);
            
            boolean available = userInfoService.isPhoneAvailable(phone);
            
            Map<String, Boolean> result = new HashMap<>();
            result.put("available", available);
            
            return Result.success("检查完成", result);
            
        } catch (Exception e) {
            log.error("检查手机号可用性失败，手机号: {}", phone, e);
            return Result.error(e.getMessage());
        }
    }
} 