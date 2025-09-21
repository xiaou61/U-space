package com.xiaou.version.controller.admin;

import com.xiaou.common.annotation.Log;
import com.xiaou.common.annotation.RequireAdmin;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.core.domain.Result;
import com.xiaou.version.dto.VersionHistoryCreateRequest;
import com.xiaou.version.dto.VersionHistoryQueryRequest;
import com.xiaou.version.dto.VersionHistoryResponse;
import com.xiaou.version.dto.VersionHistoryUpdateRequest;
import com.xiaou.version.service.VersionHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 版本历史管理端Controller
 * 
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/admin/version")
@RequiredArgsConstructor
public class VersionHistoryAdminController {
    
    private final VersionHistoryService versionHistoryService;
    
    /**
     * 分页查询版本历史列表
     */
    @RequireAdmin
    @PostMapping("/list")
    public Result<PageResult<VersionHistoryResponse>> getVersionList(@RequestBody VersionHistoryQueryRequest request) {
        PageResult<VersionHistoryResponse> result = versionHistoryService.getAdminVersionList(request);
        return Result.success(result);
    }
    
    /**
     * 根据ID查询版本历史详情
     */
    @RequireAdmin
    @GetMapping("/{id}")
    public Result<VersionHistoryResponse> getVersionDetail(@PathVariable Long id) {
        VersionHistoryResponse result = versionHistoryService.getVersionDetail(id);
        return Result.success(result);
    }
    
    /**
     * 创建版本历史记录
     */
    @RequireAdmin
    @Log(module = "版本管理", type = Log.OperationType.INSERT, description = "创建版本历史记录")
    @PostMapping("/create")
    public Result<Long> createVersion(@Validated @RequestBody VersionHistoryCreateRequest request) {
        Long versionId = versionHistoryService.createVersion(request);
        return Result.success(versionId);
    }
    
    /**
     * 更新版本历史记录
     */
    @RequireAdmin
    @Log(module = "版本管理", type = Log.OperationType.UPDATE, description = "更新版本历史记录")
    @PostMapping("/update")
    public Result<Void> updateVersion(@Validated @RequestBody VersionHistoryUpdateRequest request) {
        versionHistoryService.updateVersion(request);
        return Result.success();
    }
    
    /**
     * 删除版本历史记录
     */
    @RequireAdmin
    @Log(module = "版本管理", type = Log.OperationType.DELETE, description = "删除版本历史记录")
    @PostMapping("/delete")
    public Result<Void> deleteVersion(@RequestBody Long id) {
        versionHistoryService.deleteVersion(id);
        return Result.success();
    }
    
    /**
     * 发布版本
     */
    @RequireAdmin
    @Log(module = "版本管理", type = Log.OperationType.UPDATE, description = "发布版本")
    @PostMapping("/publish")
    public Result<Void> publishVersion(@RequestBody Long id) {
        versionHistoryService.publishVersion(id);
        return Result.success();
    }
    
    /**
     * 隐藏版本
     */
    @RequireAdmin
    @Log(module = "版本管理", type = Log.OperationType.UPDATE, description = "隐藏版本")
    @PostMapping("/hide")
    public Result<Void> hideVersion(@RequestBody Long id) {
        versionHistoryService.hideVersion(id);
        return Result.success();
    }
    
    /**
     * 取消发布版本（设置为草稿）
     */
    @RequireAdmin
    @Log(module = "版本管理", type = Log.OperationType.UPDATE, description = "取消发布版本")
    @PostMapping("/unpublish")
    public Result<Void> unpublishVersion(@RequestBody Long id) {
        versionHistoryService.unpublishVersion(id);
        return Result.success();
    }
    
    /**
     * 批量发布版本
     */
    @RequireAdmin
    @Log(module = "版本管理", type = Log.OperationType.UPDATE, description = "批量发布版本")
    @PostMapping("/batch/publish")
    public Result<Void> batchPublishVersions(@RequestBody List<Long> ids) {
        versionHistoryService.batchPublishVersions(ids);
        return Result.success();
    }
    
    /**
     * 批量隐藏版本
     */
    @RequireAdmin
    @Log(module = "版本管理", type = Log.OperationType.UPDATE, description = "批量隐藏版本")
    @PostMapping("/batch/hide")
    public Result<Void> batchHideVersions(@RequestBody List<Long> ids) {
        versionHistoryService.batchHideVersions(ids);
        return Result.success();
    }
    
    /**
     * 批量删除版本
     */
    @RequireAdmin
    @Log(module = "版本管理", type = Log.OperationType.DELETE, description = "批量删除版本")
    @PostMapping("/batch/delete")
    public Result<Void> batchDeleteVersions(@RequestBody List<Long> ids) {
        versionHistoryService.batchDeleteVersions(ids);
        return Result.success();
    }
    
    /**
     * 检查版本号是否已存在
     */
    @RequireAdmin
    @GetMapping("/check-version/{versionNumber}")
    public Result<Boolean> checkVersionNumberExists(@PathVariable String versionNumber, 
                                                   @RequestParam(required = false) Long excludeId) {
        boolean exists = versionHistoryService.checkVersionNumberExists(versionNumber, excludeId);
        return Result.success(exists);
    }
} 