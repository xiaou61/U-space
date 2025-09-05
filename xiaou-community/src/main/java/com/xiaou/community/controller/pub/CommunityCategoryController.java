package com.xiaou.community.controller.pub;

import com.xiaou.common.annotation.Log;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.core.domain.Result;
import com.xiaou.community.domain.CommunityCategory;
import com.xiaou.community.dto.CommunityCategoryQueryRequest;
import com.xiaou.community.service.CommunityCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 前台分类控制器
 * 
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/community/categories")
@RequiredArgsConstructor
public class CommunityCategoryController {
    
    private final CommunityCategoryService communityCategoryService;
    
    /**
     * 分页查询启用的分类列表
     */
    @Log(module = "社区", type = Log.OperationType.SELECT, description = "分页查询分类列表")
    @PostMapping("/list")
    public Result<PageResult<CommunityCategory>> getCategoryList(@RequestBody CommunityCategoryQueryRequest request) {
        log.info("Controller收到分类列表查询请求: {}", request);
        PageResult<CommunityCategory> result = communityCategoryService.getCategoryList(request);
        log.info("Controller返回结果，total: {}", result.getTotal());
        return Result.success(result);
    }
    
    /**
     * 获取所有启用的分类（前台下拉选择用）
     */
    @Log(module = "社区", type = Log.OperationType.SELECT, description = "查询启用分类列表")
    @GetMapping
    public Result<List<CommunityCategory>> getEnabledCategories() {
        List<CommunityCategory> result = communityCategoryService.getEnabledCategories();
        return Result.success(result);
    }
} 