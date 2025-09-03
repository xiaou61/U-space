package com.xiaou.interview.controller.pub;

import com.xiaou.common.core.domain.Result;
import com.xiaou.interview.domain.InterviewCategory;
import com.xiaou.interview.service.InterviewCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 面试题分类公开接口
 *
 * @author xiaou
 */
@Tag(name = "面试题分类公开接口", description = "用户端面试题分类相关接口")
@RestController
@RequestMapping("/interview/categories")
@RequiredArgsConstructor
public class InterviewCategoryPublicController {

    private final InterviewCategoryService categoryService;

    @Operation(summary = "获取启用的分类列表")
    @GetMapping
    public Result<List<InterviewCategory>> getEnabledCategories() {
        List<InterviewCategory> categories = categoryService.getEnabledCategories();
        return Result.success(categories);
    }

    @Operation(summary = "获取分类详情")
    @GetMapping("/{id}")
    public Result<InterviewCategory> getCategoryById(@PathVariable Long id) {
        InterviewCategory category = categoryService.getCategoryById(id);
        return Result.success(category);
    }
} 