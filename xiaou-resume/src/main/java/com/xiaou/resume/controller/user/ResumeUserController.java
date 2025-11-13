package com.xiaou.resume.controller.user;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.core.domain.Result;
import com.xiaou.common.satoken.StpUserUtil;
import com.xiaou.resume.domain.ResumeInfo;
import com.xiaou.resume.dto.*;
import com.xiaou.resume.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户简历接口
 */
@Tag(name = "简历管理", description = "用户端简历能力")
@RestController
@RequestMapping("/resume")
@RequiredArgsConstructor
public class ResumeUserController {

    private final ResumeService resumeService;

    @Operation(summary = "创建简历")
    @PostMapping
    public Result<Long> createResume(@Valid @RequestBody ResumeCreateRequest request) {
        StpUserUtil.checkLogin();
        Long resumeId = resumeService.createResume(StpUserUtil.getLoginIdAsLong(), request);
        return Result.success(resumeId);
    }

    @Operation(summary = "更新简历")
    @PutMapping("/{id}")
    public Result<Void> updateResume(@PathVariable Long id,
                                     @Valid @RequestBody ResumeUpdateRequest request) {
        StpUserUtil.checkLogin();
        resumeService.updateResume(StpUserUtil.getLoginIdAsLong(), id, request);
        return Result.success();
    }

    @Operation(summary = "删除简历")
    @DeleteMapping("/{id}")
    public Result<Void> deleteResume(@PathVariable Long id) {
        StpUserUtil.checkLogin();
        resumeService.deleteResume(StpUserUtil.getLoginIdAsLong(), id);
        return Result.success();
    }

    @Operation(summary = "查询个人简历")
    @GetMapping
    public Result<PageResult<ResumeInfo>> listResumes(@Valid ResumeListQueryRequest request) {
        StpUserUtil.checkLogin();
        PageResult<ResumeInfo> page = resumeService.listUserResumes(StpUserUtil.getLoginIdAsLong(), request);
        return Result.success(page);
    }

    @Operation(summary = "预览简历")
    @GetMapping("/{id}/preview")
    public Result<ResumePreviewResponse> preview(@PathVariable Long id) {
        StpUserUtil.checkLogin();
        return Result.success(resumeService.previewResume(StpUserUtil.getLoginIdAsLong(), id));
    }

    @Operation(summary = "导出简历")
    @PostMapping("/{id}/export")
    public Result<ResumeExportResponse> export(@PathVariable Long id,
                                               @Valid @RequestBody ResumeExportRequest request) {
        StpUserUtil.checkLogin();
        return Result.success(resumeService.exportResume(StpUserUtil.getLoginIdAsLong(), id, request));
    }

    @Operation(summary = "生成分享链接")
    @PostMapping("/{id}/share")
    public Result<ResumeShareResponse> share(@PathVariable Long id) {
        StpUserUtil.checkLogin();
        return Result.success(resumeService.createShareLink(StpUserUtil.getLoginIdAsLong(), id));
    }

    @Operation(summary = "查看访问统计")
    @GetMapping("/{id}/analytics")
    public Result<ResumeAnalyticsResponse> analytics(@PathVariable Long id) {
        StpUserUtil.checkLogin();
        return Result.success(resumeService.getResumeAnalytics(StpUserUtil.getLoginIdAsLong(), id));
    }
}
