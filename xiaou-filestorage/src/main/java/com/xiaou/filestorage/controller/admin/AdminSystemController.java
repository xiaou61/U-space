package com.xiaou.filestorage.controller.admin;

import com.xiaou.common.annotation.RequireAdmin;
import com.xiaou.common.core.domain.Result;
import com.xiaou.filestorage.service.FileSystemSettingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 系统设置管理控制器
 *
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/admin/system")
public class AdminSystemController {

    @Autowired
    private FileSystemSettingService fileSystemSettingService;

    /**
     * 获取系统设置
     *
     * @return 系统设置
     */
    @GetMapping("/settings")
    @RequireAdmin
    public Result<Map<String, String>> getSystemSettings() {
        try {
            Map<String, String> settings = fileSystemSettingService.getSystemSettings();
            return Result.success(settings);
        } catch (Exception e) {
            log.error("获取系统设置失败: {}", e.getMessage(), e);
            return Result.error("获取系统设置失败: " + e.getMessage());
        }
    }

    /**
     * 更新系统设置
     *
     * @param settings 设置映射
     * @return 更新结果
     */
    @PutMapping("/settings")
    @RequireAdmin
    public Result<Boolean> updateSystemSettings(@RequestBody Map<String, String> settings) {
        try {
            boolean success = fileSystemSettingService.updateSystemSettings(settings);
            if (success) {
                return Result.success(true);
            } else {
                return Result.error("更新系统设置失败");
            }
        } catch (Exception e) {
            log.error("更新系统设置失败: {}", e.getMessage(), e);
            return Result.error("更新系统设置失败: " + e.getMessage());
        }
    }

    /**
     * 获取允许的文件类型
     *
     * @return 文件类型列表
     */
    @GetMapping("/file-types")
    @RequireAdmin
    public Result<List<String>> getFileTypes() {
        try {
            List<String> fileTypes = fileSystemSettingService.getAllowedFileTypes();
            return Result.success(fileTypes);
        } catch (Exception e) {
            log.error("获取文件类型失败: {}", e.getMessage(), e);
            return Result.error("获取文件类型失败: " + e.getMessage());
        }
    }

    /**
     * 更新文件类型白名单
     *
     * @param fileTypes 文件类型列表
     * @return 更新结果
     */
    @PutMapping("/file-types")
    @RequireAdmin
    public Result<Boolean> updateFileTypes(@RequestBody List<String> fileTypes) {
        try {
            boolean success = fileSystemSettingService.updateFileTypes(fileTypes);
            if (success) {
                return Result.success(true);
            } else {
                return Result.error("更新文件类型白名单失败");
            }
        } catch (Exception e) {
            log.error("更新文件类型白名单失败: {}", e.getMessage(), e);
            return Result.error("更新文件类型白名单失败: " + e.getMessage());
        }
    }

    /**
     * 获取系统配置摘要
     *
     * @return 配置摘要
     */
    @GetMapping("/summary")
    @RequireAdmin
    public Result<Map<String, Object>> getSystemSummary() {
        try {
            Map<String, Object> summary = Map.of(
                "maxFileSize", fileSystemSettingService.getMaxFileSize(),
                "moduleStorageQuota", fileSystemSettingService.getModuleStorageQuota(),
                "tempLinkExpireHours", fileSystemSettingService.getTempLinkExpireHours(),
                "autoBackupEnabled", fileSystemSettingService.isAutoBackupEnabled(),
                "allowedFileTypesCount", fileSystemSettingService.getAllowedFileTypes().size()
            );
            return Result.success(summary);
        } catch (Exception e) {
            log.error("获取系统配置摘要失败: {}", e.getMessage(), e);
            return Result.error("获取系统配置摘要失败: " + e.getMessage());
        }
    }
} 