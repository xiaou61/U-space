package com.xiaou.interview.mapper;

import com.xiaou.interview.domain.InterviewQuestion;
import com.xiaou.interview.dto.InterviewQuestionQueryRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 面试题目Mapper
 *
 * @author xiaou
 */
@Mapper
public interface InterviewQuestionMapper {

    /**
     * 插入题目
     */
    int insert(InterviewQuestion question);

    /**
     * 批量插入题目
     */
    int batchInsert(@Param("questions") List<InterviewQuestion> questions);

    /**
     * 根据ID删除题目
     */
    int deleteById(Long id);

    /**
     * 根据题单ID删除题目
     */
    int deleteByQuestionSetId(Long questionSetId);

    /**
     * 更新题目
     */
    int updateById(InterviewQuestion question);

    /**
     * 根据ID查询题目
     */
    InterviewQuestion selectById(Long id);

    /**
     * 根据题单ID查询题目列表
     */
    List<InterviewQuestion> selectByQuestionSetId(@Param("questionSetId") Long questionSetId);

    /**
     * 根据题单ID列表查询题目列表
     */
    List<InterviewQuestion> selectByQuestionSetIds(@Param("questionSetIds") List<Long> questionSetIds);

    /**
     * 分页查询
     */
    List<InterviewQuestion> selectPage(@Param("request") InterviewQuestionQueryRequest request);

    /**
     * 查询题目总数
     */
    long countPage(InterviewQuestionQueryRequest request);

    /**
     * 根据题单ID统计题目数量
     */
    int countByQuestionSetId(Long questionSetId);

    /**
     * 获取题单中的下一题
     */
    InterviewQuestion selectNextQuestion(@Param("questionSetId") Long questionSetId, 
                                       @Param("currentSortOrder") Integer currentSortOrder);

    /**
     * 获取题单中的上一题
     */
    InterviewQuestion selectPrevQuestion(@Param("questionSetId") Long questionSetId, 
                                       @Param("currentSortOrder") Integer currentSortOrder);

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
     * 搜索问题
     */
    List<InterviewQuestion> searchQuestions(@Param("keyword") String keyword);

    /**
     * 搜索题目总数
     */
    long countSearchQuestions(@Param("keyword") String keyword);

    /**
     * 获取题单中的最大排序号
     */
    Integer getMaxSortOrderByQuestionSetId(Long questionSetId);
} 