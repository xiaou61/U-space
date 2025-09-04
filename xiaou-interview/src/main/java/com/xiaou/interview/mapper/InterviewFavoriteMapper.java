package com.xiaou.interview.mapper;

import com.xiaou.interview.domain.InterviewFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户收藏Mapper
 *
 * @author xiaou
 */
@Mapper
public interface InterviewFavoriteMapper {

    /**
     * 插入收藏
     */
    int insert(InterviewFavorite favorite);

    /**
     * 删除收藏
     */
    int delete(@Param("userId") Long userId, 
               @Param("targetType") Integer targetType, 
               @Param("targetId") Long targetId);

    /**
     * 根据用户ID删除收藏
     */
    int deleteByUserId(Long userId);

    /**
     * 根据目标删除收藏
     */
    int deleteByTarget(@Param("targetType") Integer targetType, @Param("targetId") Long targetId);

    /**
     * 检查是否已收藏
     */
    boolean exists(@Param("userId") Long userId, 
                   @Param("targetType") Integer targetType, 
                   @Param("targetId") Long targetId);

    /**
     * 查询用户收藏的题单
     */
    List<InterviewFavorite> selectQuestionSetsByUserId(@Param("userId") Long userId);

    /**
     * 统计用户收藏的题单数量
     */
    long countQuestionSetsByUserId(Long userId);

    /**
     * 查询用户收藏的题目
     */
    List<InterviewFavorite> selectQuestionsByUserId(@Param("userId") Long userId);

    /**
     * 统计用户收藏的题目数量
     */
    long countQuestionsByUserId(Long userId);

    /**
     * 根据目标统计收藏数量
     */
    int countByTarget(@Param("targetType") Integer targetType, @Param("targetId") Long targetId);

    /**
     * 批量删除收藏
     */
    int batchDeleteByIds(@Param("ids") List<Long> ids);
} 