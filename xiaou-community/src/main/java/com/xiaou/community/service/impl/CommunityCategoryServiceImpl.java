package com.xiaou.community.service.impl;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.exception.BusinessException;
import com.xiaou.common.utils.PageHelper;
import com.xiaou.community.domain.CommunityCategory;
import com.xiaou.community.dto.AdminCategoryQueryRequest;
import com.xiaou.community.dto.CommunityCategoryCreateRequest;
import com.xiaou.community.dto.CommunityCategoryQueryRequest;
import com.xiaou.community.dto.CommunityCategoryUpdateRequest;
import com.xiaou.community.mapper.CommunityCategoryMapper;
import com.xiaou.community.service.CommunityCacheService;
import com.xiaou.community.service.CommunityCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 社区分类Service实现
 * 
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityCategoryServiceImpl implements CommunityCategoryService {
    
    private final CommunityCategoryMapper communityCategoryMapper;
    private final CommunityCacheService communityCacheService;
    
    @Override
    public PageResult<CommunityCategory> getAdminCategoryList(AdminCategoryQueryRequest request) {
        return PageHelper.doPage(request.getPageNum(), request.getPageSize(), () -> 
            communityCategoryMapper.selectAdminCategoryList(request)
        );
    }
    
    @Override
    public CommunityCategory getById(Long id) {
        if (id == null) {
            throw new BusinessException("分类ID不能为空");
        }
        
        CommunityCategory category = communityCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        
        return category;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createCategory(CommunityCategoryCreateRequest request) {
        // 检查分类名称是否已存在
        CommunityCategory existingCategory = communityCategoryMapper.selectByName(request.getName());
        if (existingCategory != null) {
            throw new BusinessException("分类名称已存在");
        }
        
        CommunityCategory category = new CommunityCategory();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSortOrder(request.getSortOrder());
        category.setStatus(1); // 默认启用
        category.setPostCount(0);
        
        int result = communityCategoryMapper.insert(category);
        if (result <= 0) {
            throw new BusinessException("创建分类失败");
        }
        
        // 清除分类缓存
        communityCacheService.evictCategories();
        
        log.info("创建分类成功，分类ID: {}, 分类名称: {}", category.getId(), category.getName());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCategory(CommunityCategoryUpdateRequest request) {
        // 检查分类是否存在
        CommunityCategory existingCategory = getById(request.getId());
        
        // 检查分类名称是否已被其他分类使用
        CommunityCategory categoryWithSameName = communityCategoryMapper.selectByName(request.getName());
        if (categoryWithSameName != null && !categoryWithSameName.getId().equals(request.getId())) {
            throw new BusinessException("分类名称已被其他分类使用");
        }
        
        CommunityCategory category = new CommunityCategory();
        category.setId(request.getId());
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSortOrder(request.getSortOrder());
        category.setStatus(request.getStatus());
        
        int result = communityCategoryMapper.updateById(category);
        if (result <= 0) {
            throw new BusinessException("更新分类失败");
        }
        
        log.info("更新分类成功，分类ID: {}", request.getId());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteCategory(Long id) {
        CommunityCategory category = getById(id);
        
        // 检查该分类下是否还有帖子
        if (category.getPostCount() != null && category.getPostCount() > 0) {
            throw new BusinessException("该分类下还有帖子，无法删除");
        }
        
        int result = communityCategoryMapper.deleteById(id);
        if (result <= 0) {
            throw new BusinessException("删除分类失败");
        }
        
        log.info("删除分类成功，分类ID: {}", id);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void toggleCategoryStatus(Long id) {
        CommunityCategory category = getById(id);
        
        // 切换状态
        int newStatus = category.getStatus() == 1 ? 0 : 1;
        category.setStatus(newStatus);
        
        int result = communityCategoryMapper.updateById(category);
        if (result <= 0) {
            throw new BusinessException("状态更新失败");
        }
        
        log.info("更新分类状态成功，分类ID: {}, 新状态: {}", id, newStatus);
    }
    
    @Override
    public List<CommunityCategory> getEnabledCategories() {
        // 先尝试从缓存获取
        List<CommunityCategory> cachedCategories = communityCacheService.getCachedCategories();
        if (cachedCategories != null) {
            log.debug("从缓存获取分类列表");
            return cachedCategories;
        }
        
        // 缓存未命中，从数据库查询
        List<CommunityCategory> categories = communityCategoryMapper.selectEnabledCategories();
        
        // 写入缓存
        if (categories != null && !categories.isEmpty()) {
            communityCacheService.cacheCategories(categories);
        }
        
        return categories;
    }
    
    @Override
    public PageResult<CommunityCategory> getCategoryList(CommunityCategoryQueryRequest request) {
        return PageHelper.doPage(request.getPageNum(), request.getPageSize(), () -> 
            communityCategoryMapper.selectCategoryList(request)
        );
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePostCount(Long categoryId, Integer count) {
        if (categoryId == null) {
            return;
        }
        
        int result = communityCategoryMapper.updatePostCount(categoryId, count);
        if (result <= 0) {
            log.warn("更新分类帖子数量失败，分类ID: {}, 变化量: {}", categoryId, count);
        }
    }
} 