package com.xiaou.interview.mapper;

import com.xiaou.interview.domain.InterviewCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 面试题分类Mapper
 *
 * @author xiaou
 */
@Mapper
public interface InterviewCategoryMapper {

    /**
     * 插入分类
     */
    int insert(InterviewCategory category);

    /**
     * 根据ID删除分类
     */
    int deleteById(Long id);

    /**
     * 更新分类
     */
    int updateById(InterviewCategory category);

    /**
     * 根据ID查询分类
     */
    InterviewCategory selectById(Long id);

    /**
     * 根据名称查询分类
     */
    InterviewCategory selectByName(String name);

    /**
     * 查询所有分类
     */
    List<InterviewCategory> selectAll();

    /**
     * 根据状态查询分类
     */
    List<InterviewCategory> selectByStatus(Integer status);

    /**
     * 更新题单数量
     */
    int updateQuestionSetCount(@Param("id") Long id, @Param("count") Integer count);

    /**
     * 检查分类名称是否存在
     */
    boolean existsByName(@Param("name") String name, @Param("excludeId") Long excludeId);
} 