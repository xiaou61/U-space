package com.xiaou.interview.mapper;

import com.xiaou.interview.domain.InterviewQuestionSet;
import com.xiaou.interview.dto.InterviewQuestionSetQueryRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 面试题单Mapper
 *
 * @author xiaou
 */
@Mapper
public interface InterviewQuestionSetMapper {

    /**
     * 插入题单
     */
    int insert(InterviewQuestionSet questionSet);

    /**
     * 根据ID删除题单
     */
    int deleteById(Long id);

    /**
     * 更新题单
     */
    int updateById(InterviewQuestionSet questionSet);

    /**
     * 根据ID查询题单
     */
    InterviewQuestionSet selectById(Long id);

    /**
     * 分页查询题单列表
     */
    List<InterviewQuestionSet> selectPage(@Param("request") InterviewQuestionSetQueryRequest request);

    /**
     * 查询题单总数
     */
    long countPage(InterviewQuestionSetQueryRequest request);

    /**
     * 查询用户的题单
     */
    List<InterviewQuestionSet> selectByCreatorId(@Param("creatorId") Long creatorId);

    /**
     * 更新题目数量
     */
    int updateQuestionCount(@Param("id") Long id, @Param("count") Integer count);

    /**
     * 增加浏览次数
     */
    int increaseViewCount(Long id);

    /**
     * 增加收藏次数
     */
    int increaseFavoriteCount(Long id);

    /**
     * 减少收藏次数
     */
    int decreaseFavoriteCount(Long id);

    /**
     * 检查用户是否有权限访问题单
     */
    boolean hasAccessPermission(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 搜索题单
     */
    List<InterviewQuestionSet> searchQuestionSets(@Param("keyword") String keyword);

    /**
     * 搜索题单总数
     */
    long countSearchQuestionSets(@Param("keyword") String keyword);

    /**
     * 根据分类ID统计题单数量
     */
    int countByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 查询公开的题单列表
     */
    List<InterviewQuestionSet> selectPublicQuestionSets(@Param("categoryId") Long categoryId);
} 