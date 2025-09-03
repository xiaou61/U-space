package com.xiaou.interview.service;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.interview.domain.InterviewQuestion;
import com.xiaou.interview.dto.InterviewQuestionQueryRequest;

import java.util.List;

/**
 * 面试题目服务接口
 *
 * @author xiaou
 */
public interface InterviewQuestionService {

    /**
     * 创建题目
     */
    Long createQuestion(InterviewQuestion question);

    /**
     * 更新题目
     */
    void updateQuestion(Long id, InterviewQuestion question);

    /**
     * 删除题目
     */
    void deleteQuestion(Long id);

    /**
     * 根据ID获取题目
     */
    InterviewQuestion getQuestionById(Long id);

    /**
     * 分页查询题目
     */
    PageResult<InterviewQuestion> getQuestions(InterviewQuestionQueryRequest request);

    /**
     * 根据题单ID获取题目列表
     */
    List<InterviewQuestion> getQuestionsBySetId(Long questionSetId);

    /**
     * 获取题单中的下一题
     */
    InterviewQuestion getNextQuestion(Long questionSetId, Integer currentSortOrder);

    /**
     * 获取题单中的上一题
     */
    InterviewQuestion getPrevQuestion(Long questionSetId, Integer currentSortOrder);

    /**
     * 增加浏览次数
     */
    void increaseViewCount(Long id);

    /**
     * 搜索题目
     */
    PageResult<InterviewQuestion> searchQuestions(String keyword, int page, int size);

    /**
     * 批量删除题目
     */
    void batchDeleteQuestions(List<Long> ids);
} 