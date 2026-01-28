package com.xiaou.interview.service.impl;

import cn.hutool.core.util.StrUtil;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.exception.BusinessException;
import com.xiaou.common.utils.PageHelper;
import com.xiaou.interview.domain.InterviewQuestion;
import com.xiaou.interview.dto.InterviewQuestionQueryRequest;
import com.xiaou.interview.dto.RandomQuestionRequest;
import com.xiaou.interview.mapper.InterviewQuestionMapper;
import com.xiaou.interview.mapper.InterviewQuestionSetMapper;
import com.xiaou.interview.service.InterviewQuestionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 面试题目服务实现类
 *
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewQuestionServiceImpl implements InterviewQuestionService {

    private final InterviewQuestionMapper questionMapper;
    private final InterviewQuestionSetMapper questionSetMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createQuestion(InterviewQuestion question) {
        // 验证题单是否存在
        if (questionSetMapper.selectById(question.getQuestionSetId()) == null) {
            throw new BusinessException("题单不存在");
        }

        // 设置排序号
        if (question.getSortOrder() == null) {
            Integer maxSortOrder = questionMapper.getMaxSortOrderByQuestionSetId(question.getQuestionSetId());
            question.setSortOrder(maxSortOrder != null ? maxSortOrder + 1 : 1);
        }

        // 设置默认值
        question.setViewCount(0)
                .setFavoriteCount(0)
                .setCreateTime(LocalDateTime.now())
                .setUpdateTime(LocalDateTime.now());

        int result = questionMapper.insert(question);
        if (result <= 0) {
            throw new BusinessException("创建题目失败");
        }

        // 更新题单的题目数量
        updateQuestionSetCount(question.getQuestionSetId());

        log.info("创建题目成功: {}", question.getTitle());
        return question.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateQuestion(Long id, InterviewQuestion question) {
        InterviewQuestion existingQuestion = questionMapper.selectById(id);
        if (existingQuestion == null) {
            throw new BusinessException("题目不存在");
        }

        // 如果更换了题单，验证新题单是否存在
        if (!existingQuestion.getQuestionSetId().equals(question.getQuestionSetId())) {
            if (questionSetMapper.selectById(question.getQuestionSetId()) == null) {
                throw new BusinessException("目标题单不存在");
            }
        }

        question.setId(id).setUpdateTime(LocalDateTime.now());

        int result = questionMapper.updateById(question);
        if (result <= 0) {
            throw new BusinessException("更新题目失败");
        }

        // 如果更换了题单，需要更新两个题单的题目数量
        if (!existingQuestion.getQuestionSetId().equals(question.getQuestionSetId())) {
            updateQuestionSetCount(existingQuestion.getQuestionSetId());
            updateQuestionSetCount(question.getQuestionSetId());
        }

        log.info("更新题目成功: {}", question.getTitle());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteQuestion(Long id) {
        InterviewQuestion question = questionMapper.selectById(id);
        if (question == null) {
            throw new BusinessException("题目不存在");
        }

        int result = questionMapper.deleteById(id);
        if (result <= 0) {
            throw new BusinessException("删除题目失败");
        }

        // 更新题单的题目数量
        updateQuestionSetCount(question.getQuestionSetId());

        log.info("删除题目成功: {}", question.getTitle());
    }

    @Override
    public InterviewQuestion getQuestionById(Long id) {
        InterviewQuestion question = questionMapper.selectById(id);
        if (question == null) {
            throw new BusinessException("题目不存在");
        }
        return question;
    }

    @Override
    public PageResult<InterviewQuestion> getQuestions(InterviewQuestionQueryRequest request) {
        return PageHelper.doPage(request.getPageNum(), request.getPageSize(), () -> 
            questionMapper.selectPage(request)
        );
    }

    @Override
    public List<InterviewQuestion> getQuestionsBySetId(Long questionSetId) {
        return questionMapper.selectByQuestionSetId(questionSetId);
    }

    @Override
    public InterviewQuestion getNextQuestion(Long questionSetId, Integer currentSortOrder) {
        return questionMapper.selectNextQuestion(questionSetId, currentSortOrder);
    }

    @Override
    public InterviewQuestion getPrevQuestion(Long questionSetId, Integer currentSortOrder) {
        return questionMapper.selectPrevQuestion(questionSetId, currentSortOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void increaseViewCount(Long id) {
        int result = questionMapper.increaseViewCount(id);
        if (result <= 0) {
            log.warn("增加题目浏览次数失败: id={}", id);
        }
    }

    @Override
    public PageResult<InterviewQuestion> searchQuestions(String keyword, int page, int size) {
        if (StrUtil.isBlank(keyword)) {
            return PageResult.of(page, size, 0L, Collections.emptyList());
        }

        return PageHelper.doPage(page, size, () -> 
            questionMapper.searchQuestions(keyword)
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteQuestions(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        // 批量查询要删除的题目信息，用于后续更新题单数量
        List<InterviewQuestion> questions = questionMapper.selectBatchIds(ids);

        if (questions.isEmpty()) {
            return;
        }

        // 批量删除题目
        questionMapper.deleteBatchIds(ids);

        // 更新相关题单的题目数量
        questions.stream()
                .map(InterviewQuestion::getQuestionSetId)
                .distinct()
                .forEach(this::updateQuestionSetCount);

        log.info("批量删除题目成功: count={}", questions.size());
    }

    @Override
    public List<InterviewQuestion> getRandomQuestions(RandomQuestionRequest request) {
        // 验证请求参数
        if (request == null || request.getQuestionSetIds() == null || request.getQuestionSetIds().isEmpty()) {
            throw new BusinessException("题单ID列表不能为空");
        }
        
        if (request.getCount() == null || request.getCount() <= 0) {
            throw new BusinessException("抽题数量必须大于0");
        }

        // 验证题单是否存在且为公开状态
        for (Long questionSetId : request.getQuestionSetIds()) {
            if (questionSetMapper.selectById(questionSetId) == null) {
                throw new BusinessException("题单不存在: " + questionSetId);
            }
        }

        // 获取所有指定题单的题目
        List<InterviewQuestion> allQuestions = questionMapper.selectByQuestionSetIds(request.getQuestionSetIds());
        if (allQuestions.isEmpty()) {
            return Collections.emptyList();
        }

        // 如果请求的数量大于等于总题目数，返回所有题目并打乱顺序
        if (request.getCount() >= allQuestions.size()) {
            Collections.shuffle(allQuestions);
            return allQuestions;
        }

        // 随机选择指定数量的题目
        Collections.shuffle(allQuestions, new Random());
        return allQuestions.subList(0, request.getCount());
    }

    /**
     * 更新题单的题目数量
     */
    private void updateQuestionSetCount(Long questionSetId) {
        int count = questionMapper.countByQuestionSetId(questionSetId);
        questionSetMapper.updateQuestionCount(questionSetId, count);
    }
} 