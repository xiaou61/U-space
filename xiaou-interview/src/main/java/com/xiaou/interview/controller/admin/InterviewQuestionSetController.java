package com.xiaou.interview.controller.admin;

import com.xiaou.common.annotation.Log;
import com.xiaou.common.annotation.RequireAdmin;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.core.domain.Result;
import com.xiaou.interview.domain.InterviewQuestionSet;
import com.xiaou.interview.dto.InterviewQuestionSetQueryRequest;
import com.xiaou.interview.dto.InterviewQuestionSetRequest;
import com.xiaou.interview.dto.MarkdownImportRequest;
import com.xiaou.interview.service.InterviewQuestionSetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 面试题单管理接口
 *
 * @author xiaou
 */
@Tag(name = "面试题单管理", description = "面试题单增删改查、Markdown导入等功能")
@RestController
@RequestMapping("/admin/interview/question-sets")
@RequiredArgsConstructor
public class InterviewQuestionSetController {

    private final InterviewQuestionSetService questionSetService;

    @Operation(summary = "创建题单")
    @PostMapping
    @RequireAdmin
    @Log(module = "面试题单", type = Log.OperationType.INSERT, description = "创建题单")
    public Result<Long> createQuestionSet(@Valid @RequestBody InterviewQuestionSetRequest request) {
        Long id = questionSetService.createQuestionSet(request);
        return Result.success(id);
    }

    @Operation(summary = "更新题单")
    @PutMapping("/{id}")
    @RequireAdmin
    @Log(module = "面试题单", type = Log.OperationType.UPDATE, description = "更新题单")
    public Result<Void> updateQuestionSet(@PathVariable Long id, 
                                          @Valid @RequestBody InterviewQuestionSetRequest request) {
        questionSetService.updateQuestionSet(id, request);
        return Result.success();
    }

    @Operation(summary = "删除题单")
    @DeleteMapping("/{id}")
    @RequireAdmin
    @Log(module = "面试题单", type = Log.OperationType.DELETE, description = "删除题单")
    public Result<Void> deleteQuestionSet(@PathVariable Long id) {
        questionSetService.deleteQuestionSet(id);
        return Result.success();
    }

    @Operation(summary = "获取题单详情")
    @GetMapping("/{id}")
    @RequireAdmin
    public Result<InterviewQuestionSet> getQuestionSet(@PathVariable Long id) {
        InterviewQuestionSet questionSet = questionSetService.getQuestionSetById(id);
        return Result.success(questionSet);
    }

    @Operation(summary = "分页查询题单")
    @PostMapping("/list")
    @RequireAdmin
    public Result<PageResult<InterviewQuestionSet>> getQuestionSets(@RequestBody InterviewQuestionSetQueryRequest request) {
        PageResult<InterviewQuestionSet> result = questionSetService.getQuestionSets(request);
        return Result.success(result);
    }

    @Operation(summary = "获取用户的题单")
    @GetMapping("/user/{userId}")
    @RequireAdmin
    public Result<List<InterviewQuestionSet>> getUserQuestionSets(@PathVariable Long userId) {
        List<InterviewQuestionSet> questionSets = questionSetService.getUserQuestionSets(userId);
        return Result.success(questionSets);
    }

    @Operation(summary = "导入Markdown题目")
    @PostMapping("/import")
    @RequireAdmin
    @Log(module = "面试题单", type = Log.OperationType.IMPORT, description = "导入Markdown题目")
    public Result<Integer> importMarkdownQuestions(@Valid @RequestBody MarkdownImportRequest request) {
        int count = questionSetService.importMarkdownQuestions(request);
        return Result.success(count);
    }

    @Operation(summary = "增加浏览次数")
    @PostMapping("/{id}/view")
    @RequireAdmin
    public Result<Void> increaseViewCount(@PathVariable Long id) {
        questionSetService.increaseViewCount(id);
        return Result.success();
    }

    @Operation(summary = "搜索题单")
    @GetMapping("/search")
    @RequireAdmin
    public Result<PageResult<InterviewQuestionSet>> searchQuestionSets(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<InterviewQuestionSet> result = questionSetService.searchQuestionSets(keyword, page, size);
        return Result.success(result);
    }
} 