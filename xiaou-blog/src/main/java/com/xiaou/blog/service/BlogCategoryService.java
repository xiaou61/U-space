package com.xiaou.blog.service;

import com.xiaou.blog.domain.BlogCategory;
import com.xiaou.blog.dto.CategoryCreateRequest;
import com.xiaou.blog.dto.CategoryUpdateRequest;
import com.xiaou.common.core.domain.PageResult;

import java.util.List;

/**
 * 博客分类Service接口
 * 
 * @author xiaou
 */
public interface BlogCategoryService {
    
    /**
     * 获取所有分类列表
     */
    List<BlogCategory> getAllCategories();
    
    /**
     * 分页获取分类列表
     */
    PageResult<BlogCategory> getCategoryList(Integer pageNum, Integer pageSize);
    
    /**
     * 根据ID获取分类
     */
    BlogCategory getCategoryById(Long id);
    
    /**
     * 创建分类
     */
    void createCategory(CategoryCreateRequest request);
    
    /**
     * 更新分类
     */
    void updateCategory(Long id, CategoryUpdateRequest request);
    
    /**
     * 删除分类
     */
    void deleteCategory(Long id);
}

