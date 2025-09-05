package com.xiaou.community.service;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.community.domain.CommunityCategory;
import com.xiaou.community.dto.AdminCategoryQueryRequest;
import com.xiaou.community.dto.CommunityCategoryCreateRequest;
import com.xiaou.community.dto.CommunityCategoryQueryRequest;
import com.xiaou.community.dto.CommunityCategoryUpdateRequest;

import java.util.List;

/**
 * 社区分类Service接口
 * 
 * @author xiaou
 */
public interface CommunityCategoryService {
    
    /**
     * 管理端查询分类列表
     */
    PageResult<CommunityCategory> getAdminCategoryList(AdminCategoryQueryRequest request);
    
    /**
     * 根据ID查询分类
     */
    CommunityCategory getById(Long id);
    
    /**
     * 创建分类
     */
    void createCategory(CommunityCategoryCreateRequest request);
    
    /**
     * 更新分类
     */
    void updateCategory(CommunityCategoryUpdateRequest request);
    
    /**
     * 删除分类
     */
    void deleteCategory(Long id);
    
    /**
     * 启用/禁用分类
     */
    void toggleCategoryStatus(Long id);
    
    /**
     * 获取所有启用的分类（前台用）
     */
    List<CommunityCategory> getEnabledCategories();
    
    /**
     * 前台分页查询启用的分类列表
     */
    PageResult<CommunityCategory> getCategoryList(CommunityCategoryQueryRequest request);
    
    /**
     * 更新分类下的帖子数量
     */
    void updatePostCount(Long categoryId, Integer count);
} 