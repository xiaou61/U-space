package com.xiaou.community.controller.pub;

import com.xiaou.common.annotation.Log;
import com.xiaou.common.core.domain.Result;
import com.xiaou.community.domain.CommunityCategory;
import com.xiaou.community.service.CommunityCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
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
     * 获取所有启用的分类（前台下拉选择用）
     */
    @Log(module = "社区", type = Log.OperationType.SELECT, description = "查询启用分类列表")
    @GetMapping
    public Result<List<CommunityCategory>> getEnabledCategories() {
        List<CommunityCategory> result = communityCategoryService.getEnabledCategories();
        return Result.success(result);
    }
} 