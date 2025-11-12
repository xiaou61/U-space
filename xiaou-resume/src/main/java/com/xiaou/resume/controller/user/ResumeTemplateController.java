package com.xiaou.resume.controller.user;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.core.domain.Result;
import com.xiaou.resume.domain.ResumeTemplate;
import com.xiaou.resume.dto.TemplateQueryRequest;
import com.xiaou.resume.service.ResumeTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户模板接口
 */
@Tag(name = "简历模板", description = "用户模板查询")
@RestController
@RequestMapping("/resume/templates")
@RequiredArgsConstructor
public class ResumeTemplateController {

    private final ResumeTemplateService resumeTemplateService;

    @Operation(summary = "查询模板列表")
    @GetMapping
    public Result<PageResult<ResumeTemplate>> listTemplates(@Valid TemplateQueryRequest request) {
        return Result.success(resumeTemplateService.listTemplates(request));
    }

    @Operation(summary = "获取模板详情")
    @GetMapping("/{id}")
    public Result<ResumeTemplate> getTemplate(@PathVariable Long id) {
        return Result.success(resumeTemplateService.getTemplate(id));
    }
}
