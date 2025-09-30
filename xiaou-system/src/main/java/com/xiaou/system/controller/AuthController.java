package com.xiaou.system.controller;

import com.xiaou.common.annotation.Log;
import com.xiaou.common.annotation.RequireAdmin;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.core.domain.Result;
import com.xiaou.common.core.domain.ResultCode;
import com.xiaou.common.satoken.StpAdminUtil;
import com.xiaou.system.domain.SysAdmin;
import com.xiaou.system.dto.*;
import com.xiaou.system.service.SysAdminService;
import com.xiaou.system.service.SysLoginLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    @Log(module = "用户管理", type = Log.OperationType.OTHER, description = "用户登录", saveResponseData = false)
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
    @Log(module = "用户管理", type = Log.OperationType.OTHER, description = "用户登出")
    public Result<?> logout() {
        try {
            // 获取当前管理员ID
            Long adminId = StpAdminUtil.getLoginIdAsLong();
            log.info("🚪 管理员登出，管理员ID: {}", adminId);
            
            // 使用 Sa-Token 登出（自动清除 Session 和 Token）
            StpAdminUtil.logout();
            
            return Result.success("登出成功");
        } catch (Exception e) {
            log.error("登出失败", e);
            return Result.error("登出失败");
        }
    }

    /**
     * 刷新令牌（Sa-Token 自动续签，此接口可选）
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
    public Result<String> refresh() {
        try {
            if (!StpAdminUtil.isLogin()) {
                return Result.error(ResultCode.TOKEN_INVALID.getCode(), "Token无效或已过期");
            }
            
            // Sa-Token 会自动续签，这里直接返回当前 Token
            String token = StpAdminUtil.getTokenValue();
            
            log.info("🔄 令牌刷新成功");
            return Result.success("刷新成功", token);
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
    @RequireAdmin(message = "获取用户信息需要管理员权限")
    @GetMapping("/info")
    public Result<LoginResponse.UserInfo> info() {
        try {
            // 通过AOP自动验证管理员权限，直接获取管理员ID
            Long adminId = StpAdminUtil.getLoginIdAsLong();
            
            // 从数据库获取管理员信息
            SysAdmin admin = adminService.getById(adminId);
            if (admin == null) {
                return Result.error("用户信息不存在");
            }
            
            // 构建用户信息响应
            LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo();
            userInfo.setId(admin.getId())
                    .setUsername(admin.getUsername())
                    .setRealName(admin.getRealName())
                    .setEmail(admin.getEmail())
                    .setAvatar(admin.getAvatar())
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
    @RequireAdmin(message = "查询登录日志需要管理员权限")
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
    @RequireAdmin(message = "查询登录日志详情需要管理员权限")
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
    @RequireAdmin(message = "清空登录日志需要管理员权限")
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

    /**
     * 更新个人信息
     */
    @Operation(summary = "更新个人信息", description = "更新当前登录用户的个人信息，不包括密码")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "更新成功"),
            @ApiResponse(responseCode = "701", description = "Token无效"),
            @ApiResponse(responseCode = "702", description = "用户信息已过期"),
            @ApiResponse(responseCode = "409", description = "邮箱已被其他用户使用"),
            @ApiResponse(responseCode = "500", description = "更新失败")
    })
    @SecurityRequirement(name = "Bearer Token")
    @RequireAdmin(message = "更新个人信息需要管理员权限")
    @PutMapping("/profile")
    @Log(module = "用户管理", type = Log.OperationType.UPDATE, description = "更新个人信息")
    public Result<?> updateProfile(@Parameter(description = "更新个人信息请求", required = true)
            @Valid @RequestBody UpdateAdminRequest request) {
        try {
            // 通过AOP自动验证管理员权限，直接获取管理员ID
            Long adminId = StpAdminUtil.getLoginIdAsLong();
            
            boolean success = adminService.updateCurrentUserInfo(adminId, request);
            if (success) {
                log.info("✅ 用户个人信息更新成功，管理员ID: {}", adminId);
                return Result.success("个人信息更新成功");
            } else {
                return Result.error("个人信息更新失败");
            }
        } catch (Exception e) {
            log.error("更新个人信息失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 修改密码
     */
    @Operation(summary = "修改密码", description = "修改当前登录用户的密码，需要验证原密码")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "密码修改成功"),
            @ApiResponse(responseCode = "701", description = "Token无效"),
            @ApiResponse(responseCode = "702", description = "用户信息已过期"),
            @ApiResponse(responseCode = "400", description = "原密码错误或新密码验证失败"),
            @ApiResponse(responseCode = "500", description = "密码修改失败")
    })
    @SecurityRequirement(name = "Bearer Token")
    @RequireAdmin(message = "修改密码需要管理员权限")
    @PutMapping("/password")
    @Log(module = "用户管理", type = Log.OperationType.UPDATE, description = "修改密码", saveRequestData = false)
    public Result<?> changePassword(@Parameter(description = "修改密码请求", required = true)
            @Valid @RequestBody ChangePasswordRequest request) {
        try {
            // 通过AOP自动验证管理员权限，直接获取管理员ID
            Long adminId = StpAdminUtil.getLoginIdAsLong();
            
            boolean success = adminService.changeCurrentUserPassword(adminId, request);
            if (success) {
                log.info("✅ 用户密码修改成功，管理员ID: {}", adminId);
                
                // 密码修改后，需要前端重新登录
                return Result.success("密码修改成功，请重新登录");
            } else {
                return Result.error("密码修改失败");
            }
        } catch (Exception e) {
            log.error("修改密码失败", e);
            return Result.error(e.getMessage());
        }
    }
} 