package com.xiaou.blog.controller.user;

import com.xiaou.blog.dto.*;
import com.xiaou.blog.service.BlogArticleService;
import com.xiaou.blog.service.BlogCategoryService;
import com.xiaou.blog.service.BlogConfigService;
import com.xiaou.blog.service.BlogTagService;
import com.xiaou.common.annotation.Log;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.core.domain.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 博客用户端控制器
 * 
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/user/blog")
@RequiredArgsConstructor
public class BlogUserController {
    
    private final BlogConfigService blogConfigService;
    private final BlogArticleService blogArticleService;
    private final BlogCategoryService blogCategoryService;
    private final BlogTagService blogTagService;
    
    // ========== 博客配置相关 ==========
    
    /**
     * 开通博客（扣除50积分）
     */
    @Log(module = "博客", type = Log.OperationType.INSERT, description = "开通博客")
    @PostMapping("/open")
    public Result<BlogOpenResponse> openBlog() {
        try {
            BlogOpenResponse response = blogConfigService.openBlog();
            return Result.success(response);
        } catch (Exception e) {
            log.error("开通博客失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 检查博客开通状态
     */
    @GetMapping("/check-status")
    public Result<BlogCheckStatusResponse> checkBlogStatus() {
        try {
            BlogCheckStatusResponse response = blogConfigService.checkBlogStatus();
            return Result.success(response);
        } catch (Exception e) {
            log.error("检查博客状态失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取博客配置
     */
    @GetMapping("/config/{userId}")
    public Result<BlogConfigResponse> getBlogConfig(@PathVariable Long userId) {
        try {
            BlogConfigResponse response = blogConfigService.getBlogConfigByUserId(userId);
            return Result.success(response);
        } catch (Exception e) {
            log.error("获取博客配置失败，userId: {}", userId, e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新博客配置
     */
    @Log(module = "博客", type = Log.OperationType.UPDATE, description = "更新博客配置")
    @PostMapping("/config/update")
    public Result<Void> updateBlogConfig(@Validated @RequestBody BlogConfigUpdateRequest request) {
        try {
            blogConfigService.updateBlogConfig(request);
            return Result.success();
        } catch (Exception e) {
            log.error("更新博客配置失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    // ========== 文章管理相关 ==========
    
    /**
     * 创建文章（草稿）
     */
    @Log(module = "博客", type = Log.OperationType.INSERT, description = "创建文章草稿")
    @PostMapping("/article/create")
    public Result<Long> createArticle(@Validated @RequestBody ArticlePublishRequest request) {
        try {
            Long articleId = blogArticleService.createArticle(request);
            return Result.success(articleId);
        } catch (Exception e) {
            log.error("创建文章失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 发布文章（扣除20积分）
     */
    @Log(module = "博客", type = Log.OperationType.INSERT, description = "发布文章")
    @PostMapping("/article/publish")
    public Result<ArticlePublishResponse> publishArticle(@Validated @RequestBody ArticlePublishRequest request) {
        try {
            ArticlePublishResponse response = blogArticleService.publishArticle(request);
            return Result.success(response);
        } catch (Exception e) {
            log.error("发布文章失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 更新文章
     */
    @Log(module = "博客", type = Log.OperationType.UPDATE, description = "更新文章")
    @PostMapping("/article/update/{id}")
    public Result<Void> updateArticle(@PathVariable Long id, 
                                       @Validated @RequestBody ArticlePublishRequest request) {
        try {
            blogArticleService.updateArticle(id, request);
            return Result.success();
        } catch (Exception e) {
            log.error("更新文章失败，id: {}", id, e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 删除文章
     */
    @Log(module = "博客", type = Log.OperationType.DELETE, description = "删除文章")
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
    
    /**
     * 获取文章详情
     */
    @GetMapping("/article/{id}")
    public Result<ArticleDetailResponse> getArticleDetail(@PathVariable Long id) {
        try {
            ArticleDetailResponse response = blogArticleService.getArticleDetail(id);
            return Result.success(response);
        } catch (Exception e) {
            log.error("获取文章详情失败，id: {}", id, e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取用户的文章列表
     */
    @PostMapping("/article/list")
    public Result<PageResult<ArticleSimpleResponse>> getUserArticleList(@RequestBody ArticleListRequest request) {
        try {
            PageResult<ArticleSimpleResponse> result = blogArticleService.getUserArticleList(request);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取文章列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取我的文章列表
     */
    @PostMapping("/article/my-list")
    public Result<PageResult<ArticleSimpleResponse>> getMyArticleList(@RequestBody ArticleListRequest request) {
        try {
            PageResult<ArticleSimpleResponse> result = blogArticleService.getMyArticleList(request);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取我的文章列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取我的草稿列表
     */
    @PostMapping("/article/draft-list")
    public Result<PageResult<ArticleSimpleResponse>> getMyDraftList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            PageResult<ArticleSimpleResponse> result = blogArticleService.getMyDraftList(pageNum, pageSize);
            return Result.success(result);
        } catch (Exception e) {
            log.error("获取草稿列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 按分类获取文章
     */
    @PostMapping("/article/by-category")
    public Result<PageResult<ArticleSimpleResponse>> getArticlesByCategory(
            @RequestParam Long categoryId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        try {
            ArticleListRequest request = new ArticleListRequest();
            request.setCategoryId(categoryId);
            request.setPageNum(pageNum);
            request.setPageSize(pageSize);
            PageResult<ArticleSimpleResponse> result = blogArticleService.getUserArticleList(request);
            return Result.success(result);
        } catch (Exception e) {
            log.error("按分类获取文章失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    // ========== 分类标签相关 ==========
    
    /**
     * 获取所有分类
     */
    @GetMapping("/categories")
    public Result<?> getAllCategories() {
        try {
            return Result.success(blogCategoryService.getAllCategories());
        } catch (Exception e) {
            log.error("获取分类列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取所有标签
     */
    @GetMapping("/tags")
    public Result<?> getAllTags() {
        try {
            return Result.success(blogTagService.getAllTags());
        } catch (Exception e) {
            log.error("获取标签列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取热门标签
     */
    @GetMapping("/tags/hot")
    public Result<?> getHotTags(@RequestParam(defaultValue = "10") Integer limit) {
        try {
            return Result.success(blogTagService.getHotTags(limit));
        } catch (Exception e) {
            log.error("获取热门标签失败", e);
            return Result.error(e.getMessage());
        }
    }
}

