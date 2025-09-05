package com.xiaou.community.mapper;

import com.xiaou.community.domain.CommunityCategory;
import com.xiaou.community.dto.AdminCategoryQueryRequest;
import com.xiaou.community.dto.CommunityCategoryQueryRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 社区分类Mapper
 * 
 * @author xiaou
 */
@Mapper
public interface CommunityCategoryMapper {
    
    /**
     * 插入分类
     */
    int insert(CommunityCategory category);
    
    /**
     * 根据ID查询分类
     */
    CommunityCategory selectById(Long id);
    
    /**
     * 根据名称查询分类
     */
    CommunityCategory selectByName(String name);
    
    /**
     * 更新分类
     */
    int updateById(CommunityCategory category);
    
    /**
     * 删除分类
     */
    int deleteById(Long id);
    
    /**
     * 查询所有启用的分类（用于前台下拉选择）
     */
    List<CommunityCategory> selectEnabledCategories();
    
    /**
     * 前台分页查询启用的分类列表
     */
    List<CommunityCategory> selectCategoryList(@Param("request") CommunityCategoryQueryRequest request);
    
    /**
     * 更新分类下的帖子数量
     */
    int updatePostCount(@Param("id") Long id, @Param("count") Integer count);
    
    // 管理端查询方法
    
    /**
     * 查询分类总数（管理端）
     */
    Long selectAdminCategoryCount(@Param("request") AdminCategoryQueryRequest request);
    
    /**
     * 查询分类列表（管理端）
     */
    List<CommunityCategory> selectAdminCategoryList(@Param("request") AdminCategoryQueryRequest request);
} 