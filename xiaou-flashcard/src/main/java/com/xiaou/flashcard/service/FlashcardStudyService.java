package com.xiaou.flashcard.service;

import com.xiaou.flashcard.dto.request.ReviewSubmitRequest;
import com.xiaou.flashcard.dto.request.StudyStartRequest;
import com.xiaou.flashcard.dto.response.ReviewResultResponse;
import com.xiaou.flashcard.dto.response.StudyCardResponse;
import com.xiaou.flashcard.dto.response.TodayStudyResponse;

/**
 * 闪卡学习服务接口
 *
 * @author xiaou
 */
public interface FlashcardStudyService {

    /**
     * 获取今日学习任务
     */
    TodayStudyResponse getTodayStudy(Long userId);

    /**
     * 开始学习，获取下一张闪卡
     */
    StudyCardResponse startStudy(Long userId, StudyStartRequest request);

    /**
     * 获取下一张闪卡
     */
    StudyCardResponse getNextCard(Long userId, Long deckId, String mode);

    /**
     * 提交学习反馈
     */
    ReviewResultResponse submitReview(Long userId, ReviewSubmitRequest request);

    /**
     * 初始化今日统计记录
     */
    void initTodayStats(Long userId);
}
