package com.xiaou.mockinterview.mapper;

import com.xiaou.mockinterview.domain.MockInterviewQA;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 模拟面试问答记录Mapper
 *
 * @author xiaou
 */
@Mapper
public interface MockInterviewQAMapper {

    /**
     * 插入问答记录
     */
    int insert(MockInterviewQA qa);

    /**
     * 批量插入问答记录
     */
    int batchInsert(@Param("qaList") List<MockInterviewQA> qaList);

    /**
     * 根据ID更新问答记录
     */
    int updateById(MockInterviewQA qa);

    /**
     * 根据ID查询问答记录
     */
    MockInterviewQA selectById(Long id);

    /**
     * 根据会话ID查询所有问答记录
     */
    List<MockInterviewQA> selectBySessionId(Long sessionId);

    /**
     * 根据会话ID查询主问题列表（不含追问）
     */
    List<MockInterviewQA> selectMainQuestionsBySessionId(Long sessionId);

    /**
     * 根据父问答ID查询追问列表
     */
    List<MockInterviewQA> selectFollowUpsByParentId(Long parentQaId);

    /**
     * 根据会话ID查询当前待回答的问题
     */
    MockInterviewQA selectCurrentPendingBySessionId(Long sessionId);

    /**
     * 根据会话ID删除所有问答记录
     */
    int deleteBySessionId(Long sessionId);

    /**
     * 统计会话中已回答的题目数量
     */
    int countAnsweredBySessionId(Long sessionId);

    /**
     * 统计会话中高分题目数量（>=7分）
     */
    int countHighScoreBySessionId(Long sessionId);

    /**
     * 计算会话的平均得分
     */
    Double calculateAvgScoreBySessionId(Long sessionId);

    /**
     * 根据用户ID查询历史已答题目ID列表
     */
    List<Long> selectAnsweredQuestionIdsByUserId(Long userId);
}
