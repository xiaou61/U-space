package com.xiaou.blog.mapper;

import com.xiaou.blog.domain.BlogCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 文章分类Mapper
 * 
 * @author xiaou
 */
@Mapper
public interface BlogCategoryMapper {
    
    /**
     * 插入分类
     */
    int insert(BlogCategory category);
    
    /**
     * 根据ID查询分类
     */
    BlogCategory selectById(Long id);
    
    /**
     * 更新分类
     */
    int updateById(BlogCategory category);
    
    /**
     * 删除分类
     */
    int deleteById(Long id);
    
    /**
     * 查询所有分类
     */
    List<BlogCategory> selectAll();
    
    /**
     * 查询启用的分类
     */
    List<BlogCategory> selectEnabled();
    
    /**
     * 更新分类文章数量
     */
    int updateArticleCount(@Param("id") Long id, @Param("increment") Integer increment);
    
    /**
     * 增加分类文章数量
     */
    int incrementArticleCount(@Param("categoryId") Long categoryId);
    
    /**
     * 根据分类名称查询
     */
    BlogCategory selectByName(@Param("categoryName") String categoryName);
    
    /**
     * 更新分类状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}

