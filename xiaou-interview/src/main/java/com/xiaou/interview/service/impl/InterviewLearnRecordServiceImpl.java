package com.xiaou.interview.service.impl;

import com.xiaou.interview.domain.InterviewLearnRecord;
import com.xiaou.interview.mapper.InterviewLearnRecordMapper;
import com.xiaou.interview.service.InterviewLearnRecordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 学习记录服务实现类
 *
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewLearnRecordServiceImpl implements InterviewLearnRecordService {

    private final InterviewLearnRecordMapper learnRecordMapper;

    @Async
    @Override
    public void recordLearn(Long userId, Long questionSetId, Long questionId) {
        try {
            InterviewLearnRecord record = new InterviewLearnRecord()
                    .setUserId(userId)
                    .setQuestionSetId(questionSetId)
                    .setQuestionId(questionId);
            
            // 使用 INSERT IGNORE 避免重复插入
            learnRecordMapper.insertIgnore(record);
            log.debug("记录学习: userId={}, questionSetId={}, questionId={}", userId, questionSetId, questionId);
        } catch (Exception e) {
            // 异步执行失败不影响主流程
            log.warn("异步记录学习失败: userId={}, questionId={}, error={}", userId, questionId, e.getMessage());
        }
    }

    @Override
    public List<Long> getLearnedQuestionIds(Long userId, Long questionSetId) {
        if (userId == null || questionSetId == null) {
            return Collections.emptyList();
        }
        return learnRecordMapper.selectLearnedQuestionIds(userId, questionSetId);
    }

    @Override
    public int getLearnedCount(Long userId, Long questionSetId) {
        if (userId == null || questionSetId == null) {
            return 0;
        }
        return learnRecordMapper.countByUserAndSet(userId, questionSetId);
    }

    @Override
    public int getTotalLearnedCount(Long userId) {
        if (userId == null) {
            return 0;
        }
        return learnRecordMapper.countByUser(userId);
    }

    @Override
    public boolean isLearned(Long userId, Long questionId) {
        if (userId == null || questionId == null) {
            return false;
        }
        return learnRecordMapper.exists(userId, questionId);
    }
}
