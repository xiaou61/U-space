package com.xiaou.interview.mapper;

import com.xiaou.interview.domain.InterviewLearnRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 学习记录Mapper
 *
 * @author xiaou
 */
@Mapper
public interface InterviewLearnRecordMapper {

    /**
     * 插入学习记录（忽略重复）
     */
    int insertIgnore(InterviewLearnRecord record);

    /**
     * 检查是否已学习
     */
    boolean exists(@Param("userId") Long userId, @Param("questionId") Long questionId);

    /**
     * 获取用户在某题单的已学习题目ID列表
     */
    List<Long> selectLearnedQuestionIds(@Param("userId") Long userId, 
                                        @Param("questionSetId") Long questionSetId);

    /**
     * 统计用户在某题单的学习数量
     */
    int countByUserAndSet(@Param("userId") Long userId, 
                          @Param("questionSetId") Long questionSetId);

    /**
     * 统计用户总学习数量
     */
    int countByUser(@Param("userId") Long userId);

    /**
     * 删除用户的学习记录
     */
    int deleteByUser(@Param("userId") Long userId);

    /**
     * 删除某题目的所有学习记录
     */
    int deleteByQuestion(@Param("questionId") Long questionId);
}
