package com.xiaou.interview.service;

import java.util.List;

/**
 * 学习记录服务接口
 *
 * @author xiaou
 */
public interface InterviewLearnRecordService {

    /**
     * 记录学习（看题目时调用）
     */
    void recordLearn(Long userId, Long questionSetId, Long questionId);

    /**
     * 获取某题单的已学习题目ID列表
     */
    List<Long> getLearnedQuestionIds(Long userId, Long questionSetId);

    /**
     * 获取某题单的学习进度
     */
    int getLearnedCount(Long userId, Long questionSetId);

    /**
     * 获取用户总学习数量
     */
    int getTotalLearnedCount(Long userId);

    /**
     * 检查某题目是否已学习
     */
    boolean isLearned(Long userId, Long questionId);
}
