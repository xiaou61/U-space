package com.xiaou.interview.service;

import com.xiaou.interview.domain.InterviewCategory;
import com.xiaou.interview.dto.InterviewCategoryRequest;

import java.util.List;

/**
 * 面试题分类服务接口
 *
 * @author xiaou
 */
public interface InterviewCategoryService {

    /**
     * 创建分类
     */
    Long createCategory(InterviewCategoryRequest request);

    /**
     * 更新分类
     */
    void updateCategory(Long id, InterviewCategoryRequest request);

    /**
     * 删除分类
     */
    void deleteCategory(Long id);

    /**
     * 根据ID获取分类
     */
    InterviewCategory getCategoryById(Long id);

    /**
     * 获取所有分类
     */
    List<InterviewCategory> getAllCategories();

    /**
     * 获取启用的分类
     */
    List<InterviewCategory> getEnabledCategories();

    /**
     * 更新分类的题单数量
     */
    void updateQuestionSetCount(Long categoryId);

    /**
     * 检查分类是否存在题单
     */
    boolean hasQuestionSets(Long categoryId);
} 