package com.xiaou.resume.controller.admin;

import com.xiaou.common.annotation.RequireAdmin;
import com.xiaou.common.core.domain.Result;
import com.xiaou.resume.dto.ResumeAnalyticsSummary;
import com.xiaou.resume.dto.ResumeHealthReport;
import com.xiaou.resume.service.ResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端统计接口
 */
@Tag(name = "管理端统计", description = "简历指标与巡检")
@RestController
@RequestMapping("/admin/resume")
@RequiredArgsConstructor
public class ResumeAnalyticsAdminController {

    private final ResumeService resumeService;

    @Operation(summary = "平台数据总览")
    @GetMapping("/analytics")
    @RequireAdmin
    public Result<ResumeAnalyticsSummary> analytics() {
        return Result.success(resumeService.getPlatformAnalytics());
    }

    @Operation(summary = "巡检报告")
    @GetMapping("/reports")
    @RequireAdmin
    public Result<List<ResumeHealthReport>> reports() {
        return Result.success(resumeService.getHealthReports());
    }
}
