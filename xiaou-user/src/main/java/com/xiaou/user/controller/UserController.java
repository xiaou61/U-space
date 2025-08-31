package com.xiaou.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaou.common.core.domain.Result;
import com.xiaou.common.security.JwtTokenUtil;
import com.xiaou.common.security.TokenService;
import com.xiaou.common.utils.UserContextUtil;
import com.xiaou.user.domain.UserInfo;
import com.xiaou.user.dto.UserChangePasswordRequest;
import com.xiaou.user.dto.UserInfoResponse;
import com.xiaou.user.dto.UserUpdateRequest;
import com.xiaou.user.service.UserInfoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户信息管理控制器
 *
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserInfoService userInfoService;
    private final TokenService tokenService;
    private final JwtTokenUtil jwtTokenUtil;
    private final ObjectMapper objectMapper;
    private final UserContextUtil userContextUtil;

    /**
     * 获取用户信息
     */
    @GetMapping("/{userId}")
    public Result<UserInfoResponse> getUserInfo(@PathVariable Long userId) {
        try {
            log.info("获取用户信息，用户ID: {}", userId);
            
            UserInfoResponse userInfo = userInfoService.getUserInfoById(userId);
            
            log.info("获取用户信息成功，用户ID: {}", userId);
            return Result.success("获取用户信息成功", userInfo);
            
        } catch (Exception e) {
            log.error("获取用户信息失败，用户ID: {}", userId, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取当前用户信息（通过token获取用户ID）
     */
    @GetMapping("/profile")
    public Result<UserInfoResponse> getCurrentUserInfo() {
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
            
            log.info("获取当前用户信息，用户ID: {}", currentUser.getId());
            
            UserInfoResponse userInfo = userInfoService.getUserInfoById(currentUser.getId());
            
            log.info("获取当前用户信息成功，用户ID: {}", currentUser.getId());
            return Result.success("获取用户信息成功", userInfo);
            
        } catch (Exception e) {
            log.error("获取当前用户信息失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{userId}")
    public Result<UserInfoResponse> updateUserInfo(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest request) {
        try {
            log.info("更新用户信息，用户ID: {}", userId);
            
            UserInfoResponse userInfo = userInfoService.updateUserInfo(userId, request);
            
            log.info("更新用户信息成功，用户ID: {}", userId);
            return Result.success("用户信息更新成功", userInfo);
            
        } catch (Exception e) {
            log.error("更新用户信息失败，用户ID: {}", userId, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新当前用户信息（通过token获取用户ID）
     */
    @PutMapping("/profile")
    public Result<UserInfoResponse> updateCurrentUserInfo(@Valid @RequestBody UserUpdateRequest request) {
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
            
            log.info("更新当前用户信息，用户ID: {}", currentUser.getId());
            
            UserInfoResponse userInfo = userInfoService.updateUserInfo(currentUser.getId(), request);
            
            log.info("更新当前用户信息成功，用户ID: {}", currentUser.getId());
            return Result.success("用户信息更新成功", userInfo);
            
        } catch (Exception e) {
            log.error("更新当前用户信息失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 修改密码
     */
    @PutMapping("/{userId}/password")
    public Result<Void> changePassword(
            @PathVariable Long userId,
            @Valid @RequestBody UserChangePasswordRequest request) {
        try {
            log.info("修改用户密码，用户ID: {}", userId);
            
            userInfoService.changePassword(userId, request);
            
            log.info("修改用户密码成功，用户ID: {}", userId);
            return Result.success("密码修改成功", null);
            
        } catch (Exception e) {
            log.error("修改用户密码失败，用户ID: {}", userId, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 修改当前用户密码（通过token获取用户ID）
     */
    @PutMapping("/password")
    public Result<Void> changeCurrentUserPassword(
            @Valid @RequestBody UserChangePasswordRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = jwtTokenUtil.getTokenFromHeader(authHeader);
            if (token == null) {
                return Result.error("Token无效");
            }
            
            // 从Redis中获取用户信息
            String userInfoJson = tokenService.getUserFromToken(token, "user");
            if (userInfoJson == null) {
                return Result.error("用户信息已过期，请重新登录");
            }
            
            UserInfo user = objectMapper.readValue(userInfoJson, UserInfo.class);
            log.info("修改当前用户密码，用户ID: {}", user.getId());
            
            userInfoService.changePassword(user.getId(), request);
            
            // 密码修改后，将当前Token加入黑名单，强制重新登录
            tokenService.addToBlacklist(token, "user");
            tokenService.deleteToken(token, "user");
            
            log.info("修改当前用户密码成功，用户ID: {}", user.getId());
            return Result.success("密码修改成功，请重新登录", null);
            
        } catch (Exception e) {
            log.error("修改当前用户密码失败", e);
            return Result.error(e.getMessage());
        }
    }
} 