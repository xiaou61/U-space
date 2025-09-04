package com.xiaou.interview.controller.admin;

import com.xiaou.common.annotation.Log;
import com.xiaou.common.annotation.RequireAdmin;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.core.domain.Result;
import com.xiaou.interview.domain.InterviewQuestion;
import com.xiaou.interview.dto.InterviewQuestionQueryRequest;
import com.xiaou.interview.service.InterviewQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 面试题目管理接口
 *
 * @author xiaou
 */
@Tag(name = "面试题目管理", description = "面试题目增删改查功能")
@RestController
@RequestMapping("/admin/interview/questions")
@RequiredArgsConstructor
public class InterviewQuestionController {

    private final InterviewQuestionService questionService;

    @Operation(summary = "创建题目")
    @PostMapping
    @RequireAdmin
    @Log(module = "面试题目", type = Log.OperationType.INSERT, description = "创建题目")
    public Result<Long> createQuestion(@Valid @RequestBody InterviewQuestion question) {
        Long id = questionService.createQuestion(question);
        return Result.success(id);
    }

    @Operation(summary = "更新题目")
    @PutMapping("/{id}")
    @RequireAdmin
    @Log(module = "面试题目", type = Log.OperationType.UPDATE, description = "更新题目")
    public Result<Void> updateQuestion(@PathVariable Long id, 
                                       @Valid @RequestBody InterviewQuestion question) {
        questionService.updateQuestion(id, question);
        return Result.success();
    }

    @Operation(summary = "删除题目")
    @DeleteMapping("/{id}")
    @RequireAdmin
    @Log(module = "面试题目", type = Log.OperationType.DELETE, description = "删除题目")
    public Result<Void> deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return Result.success();
    }

    @Operation(summary = "获取题目详情")
    @GetMapping("/{id}")
    @RequireAdmin
    public Result<InterviewQuestion> getQuestion(@PathVariable Long id) {
        InterviewQuestion question = questionService.getQuestionById(id);
        return Result.success(question);
    }

    @Operation(summary = "分页查询题目")
    @PostMapping("/list")
    @RequireAdmin
    public Result<PageResult<InterviewQuestion>> getQuestions(@RequestBody InterviewQuestionQueryRequest request) {
        PageResult<InterviewQuestion> result = questionService.getQuestions(request);
        return Result.success(result);
    }

    @Operation(summary = "获取题单的题目列表")
    @GetMapping("/set/{questionSetId}")
    @RequireAdmin
    public Result<List<InterviewQuestion>> getQuestionsBySetId(@PathVariable Long questionSetId) {
        List<InterviewQuestion> questions = questionService.getQuestionsBySetId(questionSetId);
        return Result.success(questions);
    }

    @Operation(summary = "获取下一题")
    @GetMapping("/set/{questionSetId}/next")
    @RequireAdmin
    public Result<InterviewQuestion> getNextQuestion(@PathVariable Long questionSetId, 
                                                     @RequestParam Integer currentSortOrder) {
        InterviewQuestion question = questionService.getNextQuestion(questionSetId, currentSortOrder);
        return Result.success(question);
    }

    @Operation(summary = "获取上一题")
    @GetMapping("/set/{questionSetId}/prev")
    @RequireAdmin
    public Result<InterviewQuestion> getPrevQuestion(@PathVariable Long questionSetId, 
                                                     @RequestParam Integer currentSortOrder) {
        InterviewQuestion question = questionService.getPrevQuestion(questionSetId, currentSortOrder);
        return Result.success(question);
    }

    @Operation(summary = "增加浏览次数")
    @PostMapping("/{id}/view")
    @RequireAdmin
    public Result<Void> increaseViewCount(@PathVariable Long id) {
        questionService.increaseViewCount(id);
        return Result.success();
    }

    @Operation(summary = "搜索题目")
    @GetMapping("/search")
    @RequireAdmin
    public Result<PageResult<InterviewQuestion>> searchQuestions(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        PageResult<InterviewQuestion> result = questionService.searchQuestions(keyword, page, size);
        return Result.success(result);
    }

    @Operation(summary = "批量删除题目")
    @DeleteMapping("/batch")
    @RequireAdmin
    @Log(module = "面试题目", type = Log.OperationType.DELETE, description = "批量删除题目")
    public Result<Void> batchDeleteQuestions(@RequestBody List<Long> ids) {
        questionService.batchDeleteQuestions(ids);
        return Result.success();
    }
} 