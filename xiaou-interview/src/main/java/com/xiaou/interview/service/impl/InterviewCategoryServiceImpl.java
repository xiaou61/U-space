package com.xiaou.interview.service.impl;

import cn.hutool.core.util.StrUtil;
import com.xiaou.common.exception.BusinessException;
import com.xiaou.interview.domain.InterviewCategory;
import com.xiaou.interview.dto.InterviewCategoryRequest;
import com.xiaou.interview.mapper.InterviewCategoryMapper;
import com.xiaou.interview.mapper.InterviewQuestionSetMapper;
import com.xiaou.interview.service.InterviewCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 面试题分类服务实现类
 *
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewCategoryServiceImpl implements InterviewCategoryService {

    private final InterviewCategoryMapper categoryMapper;
    private final InterviewQuestionSetMapper questionSetMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCategory(InterviewCategoryRequest request) {
        // 验证分类名称是否重复
        if (categoryMapper.existsByName(request.getName(), null)) {
            throw new BusinessException("分类名称已存在");
        }

        InterviewCategory category = new InterviewCategory()
                .setName(request.getName())
                .setDescription(request.getDescription())
                .setSortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .setQuestionSetCount(0)
                .setStatus(request.getStatus() != null ? request.getStatus() : 1);

        int result = categoryMapper.insert(category);
        if (result <= 0) {
            throw new BusinessException("创建分类失败");
        }

        log.info("创建分类成功: {}", category.getName());
        return category.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(Long id, InterviewCategoryRequest request) {
        InterviewCategory existingCategory = categoryMapper.selectById(id);
        if (existingCategory == null) {
            throw new BusinessException("分类不存在");
        }

        // 验证分类名称是否重复（排除当前分类）
        if (categoryMapper.existsByName(request.getName(), id)) {
            throw new BusinessException("分类名称已存在");
        }

        InterviewCategory category = new InterviewCategory()
                .setId(id)
                .setName(request.getName())
                .setDescription(request.getDescription())
                .setSortOrder(request.getSortOrder())
                .setStatus(request.getStatus());

        int result = categoryMapper.updateById(category);
        if (result <= 0) {
            throw new BusinessException("更新分类失败");
        }

        log.info("更新分类成功: {}", category.getName());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        InterviewCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }

        // 检查是否存在关联的题单
        if (hasQuestionSets(id)) {
            throw new BusinessException("该分类下存在题单，无法删除");
        }

        int result = categoryMapper.deleteById(id);
        if (result <= 0) {
            throw new BusinessException("删除分类失败");
        }

        log.info("删除分类成功: {}", category.getName());
    }

    @Override
    public InterviewCategory getCategoryById(Long id) {
        InterviewCategory category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        return category;
    }

    @Override
    public List<InterviewCategory> getAllCategories() {
        return categoryMapper.selectAll();
    }

    @Override
    public List<InterviewCategory> getEnabledCategories() {
        return categoryMapper.selectByStatus(1);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateQuestionSetCount(Long categoryId) {
        int count = questionSetMapper.countByCategoryId(categoryId);
        int result = categoryMapper.updateQuestionSetCount(categoryId, count);
        if (result <= 0) {
            log.warn("更新分类题单数量失败: categoryId={}", categoryId);
        } else {
            log.info("更新分类题单数量成功: categoryId={}, count={}", categoryId, count);
        }
    }

    @Override
    public boolean hasQuestionSets(Long categoryId) {
        return questionSetMapper.countByCategoryId(categoryId) > 0;
    }
} 