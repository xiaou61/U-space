package com.xiaou.user.controller;

import com.xiaou.common.annotation.RequireAdmin;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.core.domain.Result;
import com.xiaou.common.utils.AdminContextUtil;
import com.xiaou.user.dto.AdminCreateUserRequest;
import com.xiaou.user.dto.UserInfoResponse;
import com.xiaou.user.dto.UserQueryRequest;
import com.xiaou.user.dto.UserUpdateRequest;
import com.xiaou.user.service.UserInfoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台用户管理控制器
 *
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserInfoService userInfoService;
    private final AdminContextUtil adminContextUtil;

    /**
     * 分页查询用户列表
     */
    @RequireAdmin(message = "查询用户列表需要管理员权限")
    @GetMapping("/list")
    public Result<PageResult<UserInfoResponse>> getUserList(UserQueryRequest request) {
        try {
            log.info("管理员查询用户列表，查询条件: {}", request);
            
            PageResult<UserInfoResponse> result = userInfoService.getUserList(request);
            
            log.info("管理员查询用户列表成功，总数: {}", result.getTotal());
            return Result.success("查询用户列表成功", result);
            
        } catch (Exception e) {
            log.error("管理员查询用户列表失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取所有用户（不分页，谨慎使用）
     */
    @RequireAdmin(message = "查询所有用户需要管理员权限")
    @GetMapping("/all")
    public Result<List<UserInfoResponse>> getAllUsers() {
        try {
            log.info("管理员查询所有用户");
            
            UserQueryRequest request = new UserQueryRequest();
            request.setPageNum(1);
            request.setPageSize(Integer.MAX_VALUE); // 获取所有数据
            
            PageResult<UserInfoResponse> result = userInfoService.getUserList(request);
            
            log.info("管理员查询所有用户成功，总数: {}", result.getTotal());
            return Result.success("查询所有用户成功", result.getRecords());
            
        } catch (Exception e) {
            log.error("管理员查询所有用户失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 根据ID获取用户详情
     */
    @RequireAdmin(message = "查询用户详情需要管理员权限")
    @GetMapping("/{userId}")
    public Result<UserInfoResponse> getUserInfo(@PathVariable Long userId) {
        try {
            log.info("管理员获取用户详情，用户ID: {}", userId);
            
            UserInfoResponse userInfo = userInfoService.getUserInfoById(userId);
            
            log.info("管理员获取用户详情成功，用户ID: {}", userId);
            return Result.success("获取用户详情成功", userInfo);
            
        } catch (Exception e) {
            log.error("管理员获取用户详情失败，用户ID: {}", userId, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 创建新用户
     */
    @RequireAdmin(message = "创建用户需要管理员权限")
    @PostMapping("/create")
    public Result<UserInfoResponse> createUser(
            @Valid @RequestBody AdminCreateUserRequest request) {
        try {
            // 获取当前管理员ID
            Long adminId = adminContextUtil.getCurrentAdminId();
            
            log.info("管理员创建用户，用户名: {}, 管理员ID: {}", request.getUsername(), adminId);
            
            UserInfoResponse user = userInfoService.createUser(request, adminId);
            
            log.info("管理员创建用户成功，用户ID: {}", user.getId());
            return Result.success("创建用户成功", user);
            
        } catch (Exception e) {
            log.error("管理员创建用户失败，用户名: {}", request.getUsername(), e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新用户信息
     */
    @RequireAdmin(message = "更新用户信息需要管理员权限")
    @PutMapping("/{userId}")
    public Result<UserInfoResponse> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserUpdateRequest request) {
        try {
            // 获取当前管理员ID
            Long adminId = adminContextUtil.getCurrentAdminId();
            
            log.info("管理员更新用户，用户ID: {}, 管理员ID: {}", userId, adminId);
            
            UserInfoResponse user = userInfoService.updateUserInfo(userId, request);
            
            log.info("管理员更新用户成功，用户ID: {}", userId);
            return Result.success("更新用户成功", user);
            
        } catch (Exception e) {
            log.error("管理员更新用户失败，用户ID: {}", userId, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除用户（逻辑删除）
     */
    @RequireAdmin(message = "删除用户需要管理员权限")
    @DeleteMapping("/{userId}")
    public Result<Void> deleteUser(@PathVariable Long userId) {
        try {
            // 获取当前管理员ID
            Long adminId = adminContextUtil.getCurrentAdminId();
            
            log.info("管理员删除用户，用户ID: {}, 管理员ID: {}", userId, adminId);
            
            userInfoService.deleteUser(userId, adminId);
            
            log.info("管理员删除用户成功，用户ID: {}", userId);
            return Result.success("删除用户成功", null);
            
        } catch (Exception e) {
            log.error("管理员删除用户失败，用户ID: {}", userId, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 批量删除用户（逻辑删除）
     */
    @RequireAdmin(message = "批量删除用户需要管理员权限")
    @DeleteMapping("/batch")
    public Result<Void> deleteUsers(@RequestBody List<Long> userIds) {
        try {
            // 获取当前管理员ID
            Long adminId = adminContextUtil.getCurrentAdminId();
            
            log.info("管理员批量删除用户，用户数量: {}, 管理员ID: {}", userIds.size(), adminId);
            
            userInfoService.deleteUsers(userIds, adminId);
            
            log.info("管理员批量删除用户成功，用户数量: {}", userIds.size());
            return Result.success("批量删除用户成功", null);
            
        } catch (Exception e) {
            log.error("管理员批量删除用户失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 启用/禁用用户
     */
    @RequireAdmin(message = "修改用户状态需要管理员权限")
    @PutMapping("/{userId}/status")
    public Result<Void> updateUserStatus(
            @PathVariable Long userId,
            @RequestParam Integer status) {
        try {
            // 获取当前管理员ID
            Long adminId = adminContextUtil.getCurrentAdminId();
            
            log.info("管理员修改用户状态，用户ID: {}, 状态: {}, 管理员ID: {}", userId, status, adminId);
            
            userInfoService.updateUserStatus(userId, status, adminId);
            
            String statusText = status == 0 ? "启用" : "禁用";
            log.info("管理员修改用户状态成功，用户ID: {}, 状态: {}", userId, statusText);
            return Result.success(statusText + "用户成功", null);
            
        } catch (Exception e) {
            log.error("管理员修改用户状态失败，用户ID: {}", userId, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 重置用户密码
     */
    @RequireAdmin(message = "重置用户密码需要管理员权限")
    @PutMapping("/{userId}/reset-password")
    public Result<Void> resetPassword(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "123456") String newPassword) {
        try {
            // 获取当前管理员ID
            Long adminId = adminContextUtil.getCurrentAdminId();
            
            log.info("管理员重置用户密码，用户ID: {}, 管理员ID: {}", userId, adminId);
            
            userInfoService.resetUserPassword(userId, newPassword, adminId);
            
            log.info("管理员重置用户密码成功，用户ID: {}", userId);
            return Result.success("重置密码成功，新密码: " + newPassword, null);
            
        } catch (Exception e) {
            log.error("管理员重置用户密码失败，用户ID: {}", userId, e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取用户统计信息
     */
    @RequireAdmin(message = "查看用户统计需要管理员权限")
    @GetMapping("/statistics")
    public Result<Object> getUserStatistics() {
        try {
            log.info("管理员获取用户统计信息");
            
            // 查询各种状态的用户数量
            UserQueryRequest totalRequest = new UserQueryRequest();
            totalRequest.setPageSize(1);
            PageResult<UserInfoResponse> totalResult = userInfoService.getUserList(totalRequest);
            
            UserQueryRequest activeRequest = new UserQueryRequest();
            activeRequest.setStatus(0);
            activeRequest.setPageSize(1);
            PageResult<UserInfoResponse> activeResult = userInfoService.getUserList(activeRequest);
            
            UserQueryRequest disabledRequest = new UserQueryRequest();
            disabledRequest.setStatus(1);
            disabledRequest.setPageSize(1);
            PageResult<UserInfoResponse> disabledResult = userInfoService.getUserList(disabledRequest);
            
            UserQueryRequest deletedRequest = new UserQueryRequest();
            deletedRequest.setStatus(2);
            deletedRequest.setPageSize(1);
            PageResult<UserInfoResponse> deletedResult = userInfoService.getUserList(deletedRequest);
            
            // 构建统计结果
            var statistics = new java.util.HashMap<String, Object>();
            statistics.put("totalUsers", totalResult.getTotal());
            statistics.put("activeUsers", activeResult.getTotal());
            statistics.put("disabledUsers", disabledResult.getTotal());
            statistics.put("deletedUsers", deletedResult.getTotal());
            
            log.info("管理员获取用户统计信息成功");
            return Result.success("获取用户统计信息成功", statistics);
            
        } catch (Exception e) {
            log.error("管理员获取用户统计信息失败", e);
            return Result.error(e.getMessage());
        }
    }


} 