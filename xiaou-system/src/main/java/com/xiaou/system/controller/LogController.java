package com.xiaou.system.controller;

import com.xiaou.common.annotation.Log;
import com.xiaou.common.annotation.RequireAdmin;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.core.domain.Result;
import com.xiaou.common.core.domain.ResultCode;
import com.xiaou.system.dto.*;
import com.xiaou.system.service.SysLoginLogService;
import com.xiaou.system.service.SysOperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 日志管理控制器
 *
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/log")
@RequiredArgsConstructor
@Tag(name = "日志管理", description = "登录日志、操作日志的查询和管理")
public class LogController {

    private final SysLoginLogService loginLogService;
    private final SysOperationLogService operationLogService;

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
    @PostMapping("/login")
    @Log(module = "日志管理", type = Log.OperationType.SELECT, description = "查询登录日志")
    public Result<PageResult<LoginLogResponse>> getLoginLogs(
            @Parameter(description = "查询参数") @RequestBody LoginLogQueryRequest query) {
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
    @GetMapping("/login/{id}")
    @Log(module = "日志管理", type = Log.OperationType.SELECT, description = "查询登录日志详情")
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
    @DeleteMapping("/login")
    @Log(module = "日志管理", type = Log.OperationType.CLEAN, description = "清空登录日志")
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
     * 分页查询操作日志
     */
    @Operation(summary = "分页查询操作日志", description = "支持按操作模块、操作类型、操作人、状态、时间范围等条件查询操作日志")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "500", description = "查询失败")
    })
    @SecurityRequirement(name = "Bearer Token")
    @RequireAdmin(message = "查询操作日志需要管理员权限")
    @PostMapping("/operation")
    @Log(module = "日志管理", type = Log.OperationType.SELECT, description = "查询操作日志")
    public Result<PageResult<OperationLogResponse>> getOperationLogs(
            @Parameter(description = "查询参数") @RequestBody OperationLogQueryRequest query) {
        try {
            PageResult<OperationLogResponse> result = operationLogService.getOperationLogPage(query);
            return Result.success("查询成功", result);
        } catch (Exception e) {
            log.error("查询操作日志失败", e);
            return Result.error("查询操作日志失败");
        }
    }

    /**
     * 根据ID查询操作日志详情
     */
    @Operation(summary = "查询操作日志详情", description = "根据日志ID查询操作日志的详细信息")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "查询成功"),
            @ApiResponse(responseCode = "404", description = "日志不存在"),
            @ApiResponse(responseCode = "500", description = "查询失败")
    })
    @SecurityRequirement(name = "Bearer Token")
    @RequireAdmin(message = "查询操作日志详情需要管理员权限")
    @GetMapping("/operation/{id}")
    @Log(module = "日志管理", type = Log.OperationType.SELECT, description = "查询操作日志详情")
    public Result<OperationLogResponse> getOperationLogById(
            @Parameter(description = "日志ID", required = true)
            @PathVariable Long id) {
        try {
            OperationLogResponse response = operationLogService.getById(id);
            if (response == null) {
                return Result.error(ResultCode.DATA_NOT_EXIST.getCode(), "操作日志不存在");
            }
            return Result.success("查询成功", response);
        } catch (Exception e) {
            log.error("查询操作日志详情失败", e);
            return Result.error("查询操作日志详情失败");
        }
    }

    /**
     * 批量删除操作日志
     */
    @Operation(summary = "批量删除操作日志", description = "根据ID列表批量删除操作日志")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "删除成功"),
            @ApiResponse(responseCode = "500", description = "删除失败")
    })
    @SecurityRequirement(name = "Bearer Token")
    @RequireAdmin(message = "批量删除操作日志需要管理员权限")
    @DeleteMapping("/operation")
    @Log(module = "日志管理", type = Log.OperationType.DELETE, description = "批量删除操作日志")
    public Result<?> deleteOperationLogs(
            @Parameter(description = "日志ID列表", required = true)
            @RequestBody List<Long> ids) {
        try {
            boolean success = operationLogService.deleteByIds(ids);
            if (success) {
                return Result.success("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("批量删除操作日志失败", e);
            return Result.error("批量删除操作日志失败");
        }
    }

    /**
     * 清空操作日志
     */
    @Operation(summary = "清空操作日志", description = "清空所有操作日志记录，谨慎操作")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "清空成功"),
            @ApiResponse(responseCode = "500", description = "清空失败")
    })
    @SecurityRequirement(name = "Bearer Token")
    @RequireAdmin(message = "清空操作日志需要管理员权限")
    @DeleteMapping("/operation/all")
    @Log(module = "日志管理", type = Log.OperationType.CLEAN, description = "清空操作日志")
    public Result<?> clearOperationLogs() {
        try {
            boolean success = operationLogService.clearOperationLog();
            if (success) {
                return Result.success("操作日志已清空");
            } else {
                return Result.error("清空操作日志失败");
            }
        } catch (Exception e) {
            log.error("清空操作日志失败", e);
            return Result.error("清空操作日志失败");
        }
    }

    /**
     * 清理过期操作日志
     */
    @Operation(summary = "清理过期操作日志", description = "清理指定天数之前的操作日志")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "清理成功"),
            @ApiResponse(responseCode = "500", description = "清理失败")
    })
    @SecurityRequirement(name = "Bearer Token")
    @RequireAdmin(message = "清理过期操作日志需要管理员权限")
    @DeleteMapping("/operation/clean/{days}")
    @Log(module = "日志管理", type = Log.OperationType.CLEAN, description = "清理过期操作日志")
    public Result<?> cleanOperationLogs(
            @Parameter(description = "保留天数", required = true)
            @PathVariable int days) {
        try {
            boolean success = operationLogService.cleanOperationLogByDays(days);
            if (success) {
                return Result.success("操作日志清理成功");
            } else {
                return Result.error("操作日志清理失败");
            }
        } catch (Exception e) {
            log.error("清理操作日志失败", e);
            return Result.error("清理操作日志失败");
        }
    }
} 