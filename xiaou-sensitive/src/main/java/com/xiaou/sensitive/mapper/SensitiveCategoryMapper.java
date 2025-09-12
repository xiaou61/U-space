package com.xiaou.sensitive.mapper;

import com.xiaou.sensitive.domain.SensitiveCategory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 敏感词分类数据访问接口
 */
@Mapper
public interface SensitiveCategoryMapper {

    /**
     * 查询所有启用的分类
     * @return 分类列表
     */
    List<SensitiveCategory> selectEnabledCategories();

    /**
     * 查询所有分类
     * @return 分类列表
     */
    List<SensitiveCategory> selectAllCategories();

    /**
     * 根据ID查询分类
     * @param id 分类ID
     * @return 分类信息
     */
    SensitiveCategory selectCategoryById(Integer id);
} 