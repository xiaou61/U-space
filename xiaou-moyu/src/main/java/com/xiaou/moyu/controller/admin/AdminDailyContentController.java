package com.xiaou.moyu.controller.admin;

import com.xiaou.common.annotation.Log;
import com.xiaou.common.annotation.RequireAdmin;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.core.domain.Result;
import com.xiaou.common.exception.BusinessException;
import com.xiaou.common.utils.JsonUtils;
import com.xiaou.moyu.domain.DailyContent;
import com.xiaou.moyu.dto.AdminDailyContentRequest;
import com.xiaou.moyu.service.DailyContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 后台每日内容管理控制器
 *
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/admin/moyu/daily-content")
@RequiredArgsConstructor
public class AdminDailyContentController {
    
    private final DailyContentService dailyContentService;
    
    /**
     * 获取所有内容列表（分页）
     */
    @Log(module = "摸鱼工具", type = Log.OperationType.SELECT, description = "获取内容列表")
    @GetMapping("/list")
    @RequireAdmin
    public Result<List<DailyContent>> getAllContent(@RequestParam(defaultValue = "1") Integer contentType,
                                                   @RequestParam(defaultValue = "50") Integer limit) {
        List<DailyContent> contents = dailyContentService.getAllContent(contentType, limit);
        return Result.success(contents);
    }
    
    /**
     * 根据内容类型获取内容列表
     */
    @Log(module = "摸鱼工具", type = Log.OperationType.SELECT, description = "根据类型获取内容")
    @GetMapping("/type/{contentType}")
    @RequireAdmin
    public Result<List<DailyContent>> getContentByType(@PathVariable Integer contentType,
                                                       @RequestParam(defaultValue = "50") Integer limit) {
        List<DailyContent> contents = dailyContentService.getAdminContentByType(contentType, limit);
        return Result.success(contents);
    }
    
    /**
     * 根据ID获取内容详情
     */
    @Log(module = "摸鱼工具", type = Log.OperationType.SELECT, description = "获取内容详情")
    @GetMapping("/{id}")
    @RequireAdmin
    public Result<DailyContent> getContentById(@PathVariable Long id) {
        DailyContent content = dailyContentService.getContentById(id);
        if (content == null) {
            throw new BusinessException("内容不存在");
        }
        return Result.success(content);
    }
    
    /**
     * 创建每日内容
     */
    @Log(module = "摸鱼工具", type = Log.OperationType.INSERT, description = "创建每日内容")
    @PostMapping
    @RequireAdmin
    public Result<Void> createContent(@Valid @RequestBody AdminDailyContentRequest request) {
        DailyContent content = convertToEntity(request);
        boolean success = dailyContentService.createContent(content);
        if (!success) {
            throw new BusinessException("创建内容失败");
        }
        return Result.success();
    }
    
    /**
     * 更新每日内容
     */
    @Log(module = "摸鱼工具", type = Log.OperationType.UPDATE, description = "更新每日内容")
    @PutMapping("/{id}")
    @RequireAdmin
    public Result<Void> updateContent(@PathVariable Long id, @Valid @RequestBody AdminDailyContentRequest request) {
        DailyContent content = convertToEntity(request);
        content.setId(id);
        boolean success = dailyContentService.updateContent(content);
        if (!success) {
            throw new BusinessException("更新内容失败");
        }
        return Result.success();
    }
    
    /**
     * 删除每日内容
     */
    @Log(module = "摸鱼工具", type = Log.OperationType.DELETE, description = "删除每日内容")
    @DeleteMapping("/{id}")
    @RequireAdmin
    public Result<Void> deleteContent(@PathVariable Long id) {
        boolean success = dailyContentService.deleteContent(id);
        if (!success) {
            throw new BusinessException("删除内容失败");
        }
        return Result.success();
    }
    
    /**
     * 批量删除每日内容
     */
    @Log(module = "摸鱼工具", type = Log.OperationType.DELETE, description = "批量删除每日内容")
    @PostMapping("/batch-delete")
    @RequireAdmin
    public Result<Void> batchDeleteContent(@RequestBody List<Long> ids) {
        boolean success = dailyContentService.batchDeleteContent(ids);
        if (!success) {
            throw new BusinessException("批量删除内容失败");
        }
        return Result.success();
    }
    
    /**
     * 更新内容状态
     */
    @Log(module = "摸鱼工具", type = Log.OperationType.UPDATE, description = "更新内容状态")
    @PostMapping("/{id}/status")
    @RequireAdmin
    public Result<Void> updateContentStatus(@PathVariable Long id, @RequestParam Integer status) {
        boolean success = dailyContentService.updateContentStatus(id, status);
        if (!success) {
            throw new BusinessException("更新状态失败");
        }
        return Result.success();
    }
    
    /**
     * 获取内容统计信息
     */
    @Log(module = "摸鱼工具", type = Log.OperationType.SELECT, description = "获取内容统计")
    @GetMapping("/statistics")
    @RequireAdmin
    public Result<Map<String, Object>> getContentStatistics() {
        Map<String, Object> statistics = dailyContentService.getContentStatistics();
        return Result.success(statistics);
    }
    
    /**
     * 获取用户收藏统计
     */
    @Log(module = "摸鱼工具", type = Log.OperationType.SELECT, description = "获取用户收藏统计")
    @GetMapping("/collections/statistics")
    @RequireAdmin
    public Result<Map<String, Object>> getCollectionStatistics() {
        Map<String, Object> statistics = dailyContentService.getCollectionStatistics();
        return Result.success(statistics);
    }
    
    /**
     * 获取热门内容排行
     */
    @Log(module = "摸鱼工具", type = Log.OperationType.SELECT, description = "获取热门内容排行")
    @GetMapping("/popular-ranking")
    @RequireAdmin
    public Result<List<DailyContent>> getPopularRanking(@RequestParam(defaultValue = "20") Integer limit) {
        List<DailyContent> contents = dailyContentService.getPopularContent(limit);
        return Result.success(contents);
    }
    
    /**
     * 将请求对象转换为实体对象
     */
    private DailyContent convertToEntity(AdminDailyContentRequest request) {
        DailyContent content = new DailyContent();
        content.setId(request.getId());
        content.setContentType(request.getContentType());
        content.setTitle(request.getTitle());
        content.setContent(request.getContent());
        content.setAuthor(request.getAuthor());
        content.setProgrammingLanguage(request.getProgrammingLanguage());
        content.setTags(JsonUtils.listToJson(request.getTags()));
        content.setDifficultyLevel(request.getDifficultyLevel());
        content.setSourceUrl(request.getSourceUrl());
        content.setStatus(request.getStatus());
        return content;
    }
}
