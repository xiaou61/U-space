package com.xiaou.version.controller.pub;

import com.xiaou.common.annotation.Log;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.core.domain.Result;
import com.xiaou.version.dto.VersionHistoryQueryRequest;
import com.xiaou.version.dto.VersionHistoryResponse;
import com.xiaou.version.service.VersionHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 版本历史用户端Controller
 * 
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/version")
@RequiredArgsConstructor
public class VersionHistoryController {
    
    private final VersionHistoryService versionHistoryService;
    
    /**
     * 获取版本历史时间轴（分页查询）
     */
    @Log(module = "版本历史", type = Log.OperationType.SELECT, description = "查询版本历史时间轴")
    @PostMapping("/timeline")
    public Result<PageResult<VersionHistoryResponse>> getVersionTimeline(@RequestBody VersionHistoryQueryRequest request) {
        PageResult<VersionHistoryResponse> result = versionHistoryService.getPublishedVersionList(request);
        return Result.success(result);
    }
    
    /**
     * 获取版本详情
     */
    @Log(module = "版本历史", type = Log.OperationType.SELECT, description = "查看版本详情")
    @GetMapping("/{id}")
    public Result<VersionHistoryResponse> getVersionDetail(@PathVariable Long id) {
        VersionHistoryResponse result = versionHistoryService.getVersionDetail(id);
        return Result.success(result);
    }
    
    /**
     * 记录版本查看次数
     */
    @PostMapping("/view")
    public Result<Void> recordViewCount(@RequestBody Long id) {
        versionHistoryService.incrementViewCount(id);
        return Result.success();
    }
    
    /**
     * 搜索版本历史
     */
    @Log(module = "版本历史", type = Log.OperationType.SELECT, description = "搜索版本历史")
    @PostMapping("/search")
    public Result<PageResult<VersionHistoryResponse>> searchVersions(@RequestBody VersionHistoryQueryRequest request) {
        PageResult<VersionHistoryResponse> result = versionHistoryService.getPublishedVersionList(request);
        return Result.success(result);
    }
    
    /**
     * 获取最新版本列表（用于首页展示等）
     */
    @GetMapping("/latest")
    public Result<List<VersionHistoryResponse>> getLatestVersions(@RequestParam(defaultValue = "5") int limit) {
        List<VersionHistoryResponse> result = versionHistoryService.getLatestVersions(limit);
        return Result.success(result);
    }
} 