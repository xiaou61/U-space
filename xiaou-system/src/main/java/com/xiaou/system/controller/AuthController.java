package com.xiaou.system.controller;

import com.xiaou.common.core.domain.Result;
import com.xiaou.common.core.domain.ResultCode;
import com.xiaou.system.domain.SysAdmin;
import com.xiaou.system.dto.*;
import com.xiaou.system.security.JwtTokenUtil;
import com.xiaou.system.service.SysAdminService;
import com.xiaou.system.service.SysLoginLogService;
import com.xiaou.system.service.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 认证控制器
 *
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Validated
@Tag(name = "认证管理", description = "管理员登录、登出、Token管理等认证相关接口")
public class AuthController {

    private final SysAdminService adminService;
    private final TokenService tokenService;
    private final JwtTokenUtil jwtTokenUtil;
    private final SysLoginLogService loginLogService;

    /**
     * 管理员登录
     */
    @Operation(summary = "管理员登录", description = "使用用户名和密码进行管理员登录，返回JWT Token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "登录成功"),
            @ApiResponse(responseCode = "705", description = "用户名或密码错误"),
            @ApiResponse(responseCode = "704", description = "账户已被禁用"),
            @ApiResponse(responseCode = "500", description = "系统内部错误")
    })
    @PostMapping("/login")
    public Result<LoginResponse> login(
            @Parameter(description = "登录请求参数", required = true)
            @Valid @RequestBody LoginRequest loginRequest) {
        log.info("🔐 登录请求");
        log.info("用户: {}", loginRequest.getUsername());
        LoginResponse response = adminService.login(loginRequest);
        return Result.success("登录成功", response);
    }

    /**
     * 管理员登出
     */
    @Operation(summary = "管理员登出", description = "管理员登出，将Token加入黑名单并清除用户信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "登出成功"),
            @ApiResponse(responseCode = "500", description = "登出失败")
    })
    @SecurityRequirement(name = "Bearer Token")
    @PostMapping("/logout")
    public Result<?> logout(
            @Parameter(description = "认证头，格式：Bearer {token}", required = true)
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = jwtTokenUtil.getTokenFromHeader(authHeader);
            if (token != null) {
                // 将Token加入黑名单
                tokenService.addToBlacklist(token);
                // 从Redis中删除用户信息
                tokenService.deleteToken(token);
                
                String username = tokenService.getUsernameFromToken(token);
                log.info("🚪 用户登出");
                log.info("用户: {}", username);
            }
            
            // 清除Security上下文
            SecurityContextHolder.clearContext();
            
            return Result.success();
        } catch (Exception e) {
            log.error("登出失败", e);
            return Result.error("登出失败");
        }
    }

    /**
     * 刷新令牌
     */
    @Operation(summary = "刷新令牌", description = "使用当前Token刷新获取新的Token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "刷新成功"),
            @ApiResponse(responseCode = "701", description = "Token无效"),
            @ApiResponse(responseCode = "702", description = "Token已过期"),
            @ApiResponse(responseCode = "500", description = "刷新失败")
    })
    @SecurityRequirement(name = "Bearer Token")
    @PostMapping("/refresh")
    public Result<String> refresh(
            @Parameter(description = "认证头，格式：Bearer {token}", required = true)
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = jwtTokenUtil.getTokenFromHeader(authHeader);
            if (token == null) {
                return Result.error(ResultCode.TOKEN_INVALID.getCode(), "Token无效");
            }
            
            String newToken = tokenService.refreshToken(token);
            if (newToken == null) {
                return Result.error(ResultCode.TOKEN_EXPIRED.getCode(), "Token已过期，请重新登录");
            }
            
            log.info("🔄 令牌刷新成功");
            return Result.success("刷新成功", newToken);
        } catch (Exception e) {
            log.error("刷新令牌失败", e);
            return Result.error("刷新令牌失败");
        }
    }

    /**
     * 获取用户信息
     */
    @Operation(summary = "获取用户信息", description = "根据Token获取当前登录用户的详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "获取成功"),
            @ApiResponse(responseCode = "701", description = "Token无效"),
            @ApiResponse(responseCode = "702", description = "用户信息已过期"),
            @ApiResponse(responseCode = "500", description = "获取失败")
    })
    @SecurityRequirement(name = "Bearer Token")
    @GetMapping("/info")
    public Result<LoginResponse.UserInfo> info(
            @Parameter(description = "认证头，格式：Bearer {token}", required = true)
            @RequestHeader("Authorization") String authHeader) {
        try {
            String token = jwtTokenUtil.getTokenFromHeader(authHeader);
            if (token == null) {
                return Result.error(ResultCode.TOKEN_INVALID.getCode(), "Token无效");
            }
            
            // 从Redis中获取用户信息
            SysAdmin admin = tokenService.getAdminFromToken(token);
            if (admin == null) {
                return Result.error(ResultCode.TOKEN_EXPIRED.getCode(), "用户信息已过期，请重新登录");
            }
            
            // 构建用户信息响应
            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
            userInfo.setId(admin.getId())
                    .setUsername(admin.getUsername())
                    .setRealName(admin.getRealName())
                    .setEmail(admin.getEmail())
                    .setAvatar(admin.getAvatar())
                    .setLastLoginTime(admin.getLastLoginTime())
                    .setRoles(adminService.getAdminRoles(admin.getId()))
                    .setPermissions(adminService.getAdminPermissions(admin.getId()));
            
            log.debug("获取用户信息成功: {}", admin.getUsername());
            return Result.success("获取成功", userInfo);
        } catch (Exception e) {
            log.error("获取用户信息失败", e);
            return Result.error("获取用户信息失败");
        }
    }

    /**
     * 分页查询登录日志
     */
    @Operation(summary = "分页查询登录日志", description = "支持按用户名、IP地址、登录状态、时间范围等条件查询登录日志")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "500", description = "查询失败")
    })
    @SecurityRequirement(name = "Bearer Token")
    @GetMapping("/login-logs")
    public Result<PageResult<LoginLogResponse>> getLoginLogs(
            @Parameter(description = "查询参数") LoginLogQueryRequest query) {
        try {
            PageResult<LoginLogResponse> result = loginLogService.getLoginLogPage(query);
            return Result.success("查询成功", result);
        } catch (Exception e) {
            log.error("查询登录日志失败", e);
            return Result.error("查询登录日志失败");
        }
    }

    /**
     * 根据ID查询登录日志详情
     */
    @Operation(summary = "查询登录日志详情", description = "根据日志ID查询登录日志的详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "404", description = "日志不存在"),
            @ApiResponse(responseCode = "500", description = "查询失败")
    })
    @SecurityRequirement(name = "Bearer Token")
    @GetMapping("/login-logs/{id}")
    public Result<LoginLogResponse> getLoginLogById(
            @Parameter(description = "日志ID", required = true)
            @PathVariable Long id) {
        try {
            LoginLogResponse response = loginLogService.getById(id);
            if (response == null) {
                return Result.error(ResultCode.DATA_NOT_EXIST.getCode(), "登录日志不存在");
            }
            return Result.success("查询成功", response);
        } catch (Exception e) {
            log.error("查询登录日志详情失败", e);
            return Result.error("查询登录日志详情失败");
        }
    }

    /**
     * 清空登录日志
     */
    @Operation(summary = "清空登录日志", description = "清空所有登录日志记录，谨慎操作")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "清空成功"),
            @ApiResponse(responseCode = "500", description = "清空失败")
    })
    @SecurityRequirement(name = "Bearer Token")
    @DeleteMapping("/login-logs")
    public Result<?> clearLoginLogs() {
        try {
            boolean success = loginLogService.clearLoginLog();
            if (success) {
                return Result.success("登录日志已清空");
            } else {
                return Result.error("清空登录日志失败");
            }
        } catch (Exception e) {
            log.error("清空登录日志失败", e);
            return Result.error("清空登录日志失败");
        }
    }
} 