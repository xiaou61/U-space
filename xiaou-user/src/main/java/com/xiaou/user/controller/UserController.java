package com.xiaou.user.controller;

import com.xiaou.common.core.domain.Result;
import com.xiaou.common.satoken.StpUserUtil;
import com.xiaou.user.dto.UserChangePasswordRequest;
import com.xiaou.user.dto.UserInfoResponse;
import com.xiaou.user.dto.UserUpdateRequest;
import com.xiaou.user.service.UserInfoService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.xiaou.filestorage.service.FileStorageService;
import com.xiaou.filestorage.dto.FileUploadResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 用户信息管理控制器
 *
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserInfoService userInfoService;

    @Autowired
    private FileStorageService fileStorageService;

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
            // 使用 Sa-Token 获取当前用户ID
            if (!StpUserUtil.isLogin()) {
                return Result.error("Token无效或已过期");
            }
            
            Long userId = StpUserUtil.getLoginIdAsLong();
            log.info("获取当前用户信息，用户ID: {}", userId);
            
            UserInfoResponse userInfo = userInfoService.getUserInfoById(userId);
            
            log.info("获取当前用户信息成功，用户ID: {}", userId);
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
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            
            log.info("更新当前用户信息，用户ID: {}", userId);
            
            UserInfoResponse userInfo = userInfoService.updateUserInfo(userId, request);
            
            log.info("更新当前用户信息成功，用户ID: {}", userId);
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
    public Result<Void> changeCurrentUserPassword(@Valid @RequestBody UserChangePasswordRequest request) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();
            
            log.info("修改当前用户密码，用户ID: {}", userId);
            
            userInfoService.changePassword(userId, request);
            
            // 密码修改后，退出当前登录，强制重新登录
            StpUserUtil.logout();
            
            log.info("修改当前用户密码成功，用户ID: {}", userId);
            return Result.success("密码修改成功，请重新登录", null);
            
        } catch (Exception e) {
            log.error("修改当前用户密码失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 上传头像
     */
    @PostMapping("/avatar/upload")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file) {
        try {
            if (!StpUserUtil.isLogin()) {
                return Result.error("请先登录");
            }
            Long userId = StpUserUtil.getLoginIdAsLong();

            // 文件校验
            if (file == null || file.isEmpty()) {
                return Result.error("请选择要上传的头像文件");
            }

            // 检查文件类型
            String originalName = file.getOriginalFilename();
            if (originalName == null) {
                return Result.error("文件名无效");
            }
            
            String extension = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
            if (!extension.matches("jpg|jpeg|png|gif")) {
                return Result.error("仅支持jpg、jpeg、png、gif格式的图片");
            }

            // 检查文件大小 (5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                return Result.error("头像文件大小不能超过5MB");
            }

            log.info("用户上传头像，用户ID: {}, 文件名: {}, 大小: {}KB", 
                userId, originalName, file.getSize() / 1024);

            // 上传文件
            FileUploadResult uploadResult = fileStorageService.uploadSingle(file, "user", "avatar");
            
            if (!uploadResult.isSuccess()) {
                return Result.error("头像上传失败: " + uploadResult.getErrorMessage());
            }

            // 更新用户头像
            UserUpdateRequest updateRequest = new UserUpdateRequest();
            updateRequest.setAvatar(uploadResult.getAccessUrl());
            
            userInfoService.updateUserInfo(userId, updateRequest);

            log.info("用户头像上传成功，用户ID: {}, 头像URL: {}", 
                userId, uploadResult.getAccessUrl());
            
            return Result.success("头像上传成功", uploadResult.getAccessUrl());
            
        } catch (Exception e) {
            log.error("头像上传失败", e);
            return Result.error("头像上传失败: " + e.getMessage());
        }
    }
} 