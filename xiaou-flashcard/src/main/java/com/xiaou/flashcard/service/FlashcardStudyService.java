package com.xiaou.flashcard.service;

import com.xiaou.flashcard.dto.request.FlashcardStudySubmitRequest;
import com.xiaou.flashcard.dto.response.FlashcardHeatmapVO;
import com.xiaou.flashcard.dto.response.FlashcardStudyResultVO;
import com.xiaou.flashcard.dto.response.FlashcardStudyStatsVO;
import com.xiaou.flashcard.dto.response.FlashcardStudyVO;

import java.util.List;

/**
 * 闪卡学习服务接口
 *
 * @author xiaou
 */
public interface FlashcardStudyService {

    /**
     * 获取今日待学习的卡片
     */
    List<FlashcardStudyVO> getTodayStudyCards(Long userId, Integer limit);

    /**
     * 获取指定卡组的下一张待学习卡片
     */
    FlashcardStudyVO getNextCard(Long deckId, Long userId);

    /**
     * 提交学习结果
     */
    FlashcardStudyResultVO submitStudyResult(FlashcardStudySubmitRequest request, Long userId);

    /**
     * 获取学习统计
     */
    FlashcardStudyStatsVO getStudyStats(Long userId, Long deckId);

    /**
     * 获取学习热力图数据
     */
    FlashcardHeatmapVO getHeatmap(Long userId, Integer days);
}
