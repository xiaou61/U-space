package com.xiaou.blog.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.xiaou.common.utils.PageHelper;
import com.xiaou.blog.domain.BlogCategory;
import com.xiaou.blog.dto.CategoryCreateRequest;
import com.xiaou.blog.dto.CategoryUpdateRequest;
import com.xiaou.blog.mapper.BlogCategoryMapper;
import com.xiaou.blog.service.BlogCategoryService;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 博客分类Service实现类
 * 
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BlogCategoryServiceImpl implements BlogCategoryService {
    
    private final BlogCategoryMapper blogCategoryMapper;
    
    @Override
    public List<BlogCategory> getAllCategories() {
        return blogCategoryMapper.selectAll();
    }
    
    @Override
    public PageResult<BlogCategory> getCategoryList(Integer pageNum, Integer pageSize) {
        return PageHelper.doPage(pageNum, pageSize, () -> 
            blogCategoryMapper.selectAll()
        );
    }
    
    @Override
    public BlogCategory getCategoryById(Long id) {
        return blogCategoryMapper.selectById(id);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createCategory(CategoryCreateRequest request) {
        // 检查分类名称是否已存在
        BlogCategory existCategory = blogCategoryMapper.selectByName(request.getCategoryName());
        if (existCategory != null) {
            throw new BusinessException("分类名称已存在");
        }
        
        BlogCategory category = BeanUtil.copyProperties(request, BlogCategory.class);
        category.setStatus(1);
        
        blogCategoryMapper.insert(category);
        
        log.info("创建分类成功：{}", category.getCategoryName());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(Long id, CategoryUpdateRequest request) {
        BlogCategory category = blogCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        
        // 如果修改了名称，检查是否重名
        if (request.getCategoryName() != null && !request.getCategoryName().equals(category.getCategoryName())) {
            BlogCategory existCategory = blogCategoryMapper.selectByName(request.getCategoryName());
            if (existCategory != null) {
                throw new BusinessException("分类名称已存在");
            }
        }
        
        BlogCategory updateCategory = new BlogCategory();
        updateCategory.setId(id);
        
        if (request.getCategoryName() != null) {
            updateCategory.setCategoryName(request.getCategoryName());
        }
        if (request.getCategoryIcon() != null) {
            updateCategory.setCategoryIcon(request.getCategoryIcon());
        }
        if (request.getCategoryDescription() != null) {
            updateCategory.setCategoryDescription(request.getCategoryDescription());
        }
        if (request.getSortOrder() != null) {
            updateCategory.setSortOrder(request.getSortOrder());
        }
        if (request.getStatus() != null) {
            updateCategory.setStatus(request.getStatus());
        }
        
        blogCategoryMapper.updateById(updateCategory);
        
        log.info("更新分类成功：{}", id);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        BlogCategory category = blogCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        
        // 检查是否有文章使用该分类
        if (category.getArticleCount() != null && category.getArticleCount() > 0) {
            throw new BusinessException("该分类下还有文章，无法删除");
        }
        
        blogCategoryMapper.deleteById(id);
        
        log.info("删除分类成功：{}", id);
    }
}

