package com.xiaou.interview.controller.admin;

import com.xiaou.common.annotation.RequireAdmin;
import com.xiaou.common.core.domain.Result;
import com.xiaou.interview.domain.InterviewCategory;
import com.xiaou.interview.dto.InterviewCategoryRequest;
import com.xiaou.interview.service.InterviewCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 面试题分类管理控制器
 *
 * @author xiaou
 */
@Tag(name = "面试题分类管理", description = "面试题分类相关接口")
@RestController
@RequestMapping("/admin/interview/categories")
@RequiredArgsConstructor
public class InterviewCategoryController {

    private final InterviewCategoryService categoryService;

    @Operation(summary = "创建分类")
    @PostMapping
    @RequireAdmin
    public Result<Long> createCategory(@Validated @RequestBody InterviewCategoryRequest request) {
        Long categoryId = categoryService.createCategory(request);
        return Result.success(categoryId);
    }

    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    @RequireAdmin
    public Result<Void> updateCategory(@PathVariable Long id, 
                                     @Validated @RequestBody InterviewCategoryRequest request) {
        categoryService.updateCategory(id, request);
        return Result.success();
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    @RequireAdmin
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return Result.success();
    }

    @Operation(summary = "获取分类详情")
    @GetMapping("/{id}")
    @RequireAdmin
    public Result<InterviewCategory> getCategoryById(@PathVariable Long id) {
        InterviewCategory category = categoryService.getCategoryById(id);
        return Result.success(category);
    }

    @Operation(summary = "获取所有分类")
    @GetMapping
    @RequireAdmin
    public Result<List<InterviewCategory>> getAllCategories() {
        List<InterviewCategory> categories = categoryService.getAllCategories();
        return Result.success(categories);
    }
} 