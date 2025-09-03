package com.xiaou.interview.service;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.interview.domain.InterviewQuestionSet;
import com.xiaou.interview.dto.InterviewQuestionSetQueryRequest;
import com.xiaou.interview.dto.InterviewQuestionSetRequest;
import com.xiaou.interview.dto.MarkdownImportRequest;

import java.util.List;

/**
 * 面试题单服务接口
 *
 * @author xiaou
 */
public interface InterviewQuestionSetService {

    /**
     * 创建题单
     */
    Long createQuestionSet(InterviewQuestionSetRequest request);

    /**
     * 更新题单
     */
    void updateQuestionSet(Long id, InterviewQuestionSetRequest request);

    /**
     * 删除题单
     */
    void deleteQuestionSet(Long id);

    /**
     * 根据ID获取题单
     */
    InterviewQuestionSet getQuestionSetById(Long id);

    /**
     * 分页查询题单
     */
    PageResult<InterviewQuestionSet> getQuestionSets(InterviewQuestionSetQueryRequest request);

    /**
     * 获取用户的题单
     */
    List<InterviewQuestionSet> getUserQuestionSets(Long userId);

    /**
     * 导入Markdown题目
     */
    int importMarkdownQuestions(MarkdownImportRequest request);

    /**
     * 增加浏览次数
     */
    void increaseViewCount(Long id);

    /**
     * 更新题目数量
     */
    void updateQuestionCount(Long id);

    /**
     * 检查用户是否有权限访问题单
     */
    boolean hasAccessPermission(Long questionSetId, Long userId);

    /**
     * 获取公开题单列表
     */
    PageResult<InterviewQuestionSet> getPublicQuestionSets(Long categoryId, int page, int size);

    /**
     * 搜索题单
     */
    PageResult<InterviewQuestionSet> searchQuestionSets(String keyword, int page, int size);
} 