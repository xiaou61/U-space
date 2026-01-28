package com.xiaou.user.controller;

import com.xiaou.common.core.domain.Result;
import com.xiaou.common.satoken.StpUserUtil;
import com.xiaou.common.utils.IPUtil;
import com.xiaou.user.domain.UserInfo;
import com.xiaou.user.dto.UserInfoResponse;
import com.xiaou.user.dto.UserLoginRequest;
import com.xiaou.user.dto.UserLoginResponse;
import com.xiaou.user.dto.UserRegisterRequest;
import com.xiaou.user.service.UserInfoService;
import jakarta.servlet.http.HttpServletRequest;
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
    public Result<UserLoginResponse> login(@Valid @RequestBody UserLoginRequest request, 
                                            HttpServletRequest httpRequest) {
        try {
            log.info("用户登录尝试，用户名: {}", request.getUsername());
            
            String clientIp = IPUtil.getIpAddress(httpRequest);
            UserLoginResponse response = userInfoService.login(request, clientIp);
            
            log.info("用户登录成功，用户ID: {}", response.getUserInfo().getId());
            return Result.success("登录成功", response);
            
        } catch (Exception e) {
            log.warn("用户登录失败，用户名: {}, 原因: {}", request.getUsername(), e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        try {
            // 获取当前用户ID
            Long userId = StpUserUtil.getLoginIdAsLong();
            log.info("用户登出成功，用户ID: {}", userId);
            
            // 使用 Sa-Token 登出（自动清除 Session 和 Token）
            StpUserUtil.logout();
            
            return Result.success("登出成功", null);
            
        } catch (Exception e) {
            log.error("用户登出失败", e);
            return Result.error("登出失败");
        }
    }

    /**
     * 刷新Token（Sa-Token 自动续签，此接口可选）
     */
    @PostMapping("/refresh")
    public Result<Map<String, Object>> refresh() {
        StpUserUtil.checkLogin();
        String token = StpUserUtil.getTokenValue();
        
        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", token);
        result.put("tokenType", "Bearer");
        result.put("expiresIn", StpUserUtil.getTokenTimeout());
        
        return Result.success("Token刷新成功", result);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    public Result<UserInfoResponse> getUserInfo() {
        StpUserUtil.checkLogin();
        Long userId = StpUserUtil.getLoginIdAsLong();
        UserInfoResponse userInfo = userInfoService.getUserInfoById(userId);
        return Result.success("获取成功", userInfo);
    }

    /**
     * 检查用户名是否可用
     */
    @GetMapping("/check-username")
    public Result<Map<String, Boolean>> checkUsername(@RequestParam String username) {
        boolean available = userInfoService.isUsernameAvailable(username);
        Map<String, Boolean> result = new HashMap<>();
        result.put("available", available);
        return Result.success("检查完成", result);
    }

    /**
     * 检查邮箱是否可用
     */
    @GetMapping("/check-email")
    public Result<Map<String, Boolean>> checkEmail(@RequestParam String email) {
        boolean available = userInfoService.isEmailAvailable(email);
        Map<String, Boolean> result = new HashMap<>();
        result.put("available", available);
        return Result.success("检查完成", result);
    }

    /**
     * 检查手机号是否可用
     */
    @GetMapping("/check-phone")
    public Result<Map<String, Boolean>> checkPhone(@RequestParam String phone) {
        boolean available = userInfoService.isPhoneAvailable(phone);
        Map<String, Boolean> result = new HashMap<>();
        result.put("available", available);
        return Result.success("检查完成", result);
    }
}
