package com.xiaou.community.controller.admin;

import com.xiaou.common.annotation.RequireAdmin;
import com.xiaou.common.annotation.Log;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.core.domain.Result;
import com.xiaou.community.domain.CommunityCategory;
import com.xiaou.community.dto.AdminCategoryQueryRequest;
import com.xiaou.community.dto.CommunityCategoryCreateRequest;
import com.xiaou.community.dto.CommunityCategoryUpdateRequest;
import com.xiaou.community.service.CommunityCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端分类控制器
 * 
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/admin/community/categories")
@RequiredArgsConstructor
public class AdminCommunityCategoryController {
    
    private final CommunityCategoryService communityCategoryService;
    
    /**
     * 分类列表查询
     */
    @Log(module = "社区分类管理", type = Log.OperationType.SELECT, description = "查询分类列表")
    @RequireAdmin
    @PostMapping("/list")
    public Result<PageResult<CommunityCategory>> getCategoryList(@RequestBody AdminCategoryQueryRequest request) {
        PageResult<CommunityCategory> result = communityCategoryService.getAdminCategoryList(request);
        return Result.success(result);
    }
    
    /**
     * 分类详情
     */
    @Log(module = "社区分类管理", type = Log.OperationType.SELECT, description = "查询分类详情")
    @RequireAdmin
    @GetMapping("/{id}")
    public Result<CommunityCategory> getCategoryDetail(@PathVariable Long id) {
        CommunityCategory result = communityCategoryService.getById(id);
        return Result.success(result);
    }
    
    /**
     * 创建分类
     */
    @Log(module = "社区分类管理", type = Log.OperationType.INSERT, description = "创建分类")
    @RequireAdmin
    @PostMapping
    public Result<Void> createCategory(@Validated @RequestBody CommunityCategoryCreateRequest request) {
        communityCategoryService.createCategory(request);
        return Result.success();
    }
    
    /**
     * 更新分类
     */
    @Log(module = "社区分类管理", type = Log.OperationType.UPDATE, description = "更新分类")
    @RequireAdmin
    @PutMapping("/{id}")
    public Result<Void> updateCategory(@PathVariable Long id, @Validated @RequestBody CommunityCategoryUpdateRequest request) {
        request.setId(id);
        communityCategoryService.updateCategory(request);
        return Result.success();
    }
    
    /**
     * 删除分类
     */
    @Log(module = "社区分类管理", type = Log.OperationType.DELETE, description = "删除分类")
    @RequireAdmin
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        communityCategoryService.deleteCategory(id);
        return Result.success();
    }
    
    /**
     * 启用/禁用分类
     */
    @Log(module = "社区分类管理", type = Log.OperationType.UPDATE, description = "切换分类状态")
    @RequireAdmin
    @PatchMapping("/{id}/status")
    public Result<Void> toggleCategoryStatus(@PathVariable Long id) {
        communityCategoryService.toggleCategoryStatus(id);
        return Result.success();
    }
} 