package com.xiaou.blog.controller.admin;

import com.xiaou.blog.dto.*;
import com.xiaou.blog.service.BlogArticleService;
import com.xiaou.blog.service.BlogCategoryService;
import com.xiaou.blog.service.BlogConfigService;
import com.xiaou.blog.service.BlogTagService;
import com.xiaou.common.annotation.Log;
import com.xiaou.common.annotation.RequireAdmin;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.core.domain.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 博客管理端控制器
 * 
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/admin/blog")
@RequiredArgsConstructor
public class BlogAdminController {
    
    private final BlogConfigService blogConfigService;
    private final BlogArticleService blogArticleService;
    private final BlogCategoryService blogCategoryService;
    private final BlogTagService blogTagService;
    
    // ========== 博客管理 ==========
    
    /**
     * 获取博客统计数据
     */
    @RequireAdmin
    @GetMapping("/statistics")
    public Result<BlogStatisticsResponse> getStatistics() {
        try {
            BlogStatisticsResponse response = blogConfigService.getStatistics();
            return Result.success(response);
        } catch (Exception e) {
            log.error("获取博客统计数据失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    // ========== 文章管理 ==========
    
    /**
     * 管理端获取文章列表
     */
    @RequireAdmin
    @Log(module = "博客管理", type = Log.OperationType.SELECT, description = "查询文章列表")
    @PostMapping("/article/list")
    public Result<PageResult<ArticleSimpleResponse>> getArticleList(@RequestBody AdminArticleListRequest request) {
        try {
            PageResult<ArticleSimpleResponse> result = blogArticleService.getAdminArticleList(request);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取文章列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 置顶文章
     */
    @RequireAdmin
    @Log(module = "博客管理", type = Log.OperationType.UPDATE, description = "置顶文章")
    @PostMapping("/article/top")
    public Result<Void> topArticle(@RequestParam Long id, 
                                    @RequestParam(required = false) Integer duration) {
        try {
            blogArticleService.topArticle(id, duration);
            return Result.success();
        } catch (Exception e) {
            log.error("置顶文章失败，id: {}", id, e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 取消置顶
     */
    @RequireAdmin
    @Log(module = "博客管理", type = Log.OperationType.UPDATE, description = "取消置顶")
    @PostMapping("/article/cancel-top")
    public Result<Void> cancelTop(@RequestParam Long id) {
        try {
            blogArticleService.cancelTop(id);
            return Result.success();
        } catch (Exception e) {
            log.error("取消置顶失败，id: {}", id, e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新文章状态
     */
    @RequireAdmin
    @Log(module = "博客管理", type = Log.OperationType.UPDATE, description = "更新文章状态")
    @PostMapping("/article/update-status")
    public Result<Void> updateArticleStatus(@RequestParam Long id, @RequestParam Integer status) {
        try {
            blogArticleService.updateStatus(id, status);
            return Result.success();
        } catch (Exception e) {
            log.error("更新文章状态失败，id: {}, status: {}", id, status, e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除文章
     */
    @RequireAdmin
    @Log(module = "博客管理", type = Log.OperationType.DELETE, description = "删除文章")
    @DeleteMapping("/article/{id}")
    public Result<Void> deleteArticle(@PathVariable Long id) {
        try {
            blogArticleService.deleteArticle(id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除文章失败，id: {}", id, e);
            return Result.error(e.getMessage());
        }
    }
    
    // ========== 分类管理 ==========
    
    /**
     * 获取分类列表
     */
    @RequireAdmin
    @GetMapping("/category/list")
    public Result<PageResult<?>> getCategoryList(@RequestParam(defaultValue = "1") Integer pageNum,
                                                  @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            PageResult<?> result = blogCategoryService.getCategoryList(pageNum, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取分类列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 创建分类
     */
    @RequireAdmin
    @Log(module = "博客管理", type = Log.OperationType.INSERT, description = "创建分类")
    @PostMapping("/category/create")
    public Result<Void> createCategory(@Validated @RequestBody CategoryCreateRequest request) {
        try {
            blogCategoryService.createCategory(request);
            return Result.success();
        } catch (Exception e) {
            log.error("创建分类失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新分类
     */
    @RequireAdmin
    @Log(module = "博客管理", type = Log.OperationType.UPDATE, description = "更新分类")
    @PostMapping("/category/update")
    public Result<Void> updateCategory(@RequestParam Long id, 
                                        @Validated @RequestBody CategoryUpdateRequest request) {
        try {
            blogCategoryService.updateCategory(id, request);
            return Result.success();
        } catch (Exception e) {
            log.error("更新分类失败，id: {}", id, e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除分类
     */
    @RequireAdmin
    @Log(module = "博客管理", type = Log.OperationType.DELETE, description = "删除分类")
    @DeleteMapping("/category/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        try {
            blogCategoryService.deleteCategory(id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除分类失败，id: {}", id, e);
            return Result.error(e.getMessage());
        }
    }
    
    // ========== 标签管理 ==========
    
    /**
     * 获取标签列表
     */
    @RequireAdmin
    @GetMapping("/tag/list")
    public Result<PageResult<?>> getTagList(@RequestParam(defaultValue = "1") Integer pageNum,
                                             @RequestParam(defaultValue = "20") Integer pageSize) {
        try {
            PageResult<?> result = blogTagService.getTagList(pageNum, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取标签列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 合并标签
     */
    @RequireAdmin
    @Log(module = "博客管理", type = Log.OperationType.UPDATE, description = "合并标签")
    @PostMapping("/tag/merge")
    public Result<Void> mergeTags(@RequestParam Long sourceTagId, @RequestParam Long targetTagId) {
        try {
            blogTagService.mergeTags(sourceTagId, targetTagId);
            return Result.success();
        } catch (Exception e) {
            log.error("合并标签失败，source: {}, target: {}", sourceTagId, targetTagId, e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除标签
     */
    @RequireAdmin
    @Log(module = "博客管理", type = Log.OperationType.DELETE, description = "删除标签")
    @DeleteMapping("/tag/{id}")
    public Result<Void> deleteTag(@PathVariable Long id) {
        try {
            blogTagService.deleteTag(id);
            return Result.success();
        } catch (Exception e) {
            log.error("删除标签失败，id: {}", id, e);
            return Result.error(e.getMessage());
        }
    }
}

