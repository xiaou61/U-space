package com.xiaou.flashcard.mapper;

import com.xiaou.flashcard.domain.FlashcardStudyRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 闪卡学习记录 Mapper 接口
 *
 * @author xiaou
 */
@Mapper
public interface FlashcardStudyRecordMapper {

    /**
     * 根据用户和卡片查询学习记录
     */
    FlashcardStudyRecord selectByUserAndCard(@Param("userId") Long userId,
                                              @Param("cardId") Long cardId);

    /**
     * 查询用户在某卡组的所有学习记录
     */
    List<FlashcardStudyRecord> selectByUserAndDeck(@Param("userId") Long userId,
                                                    @Param("deckId") Long deckId);

    /**
     * 查询用户待复习的卡片（下次复习时间 <= 当前时间）
     */
    List<FlashcardStudyRecord> selectDueCards(@Param("userId") Long userId,
                                               @Param("deckId") Long deckId,
                                               @Param("now") LocalDateTime now,
                                               @Param("limit") int limit);

    /**
     * 查询用户今日待复习卡片数量
     */
    int countDueCards(@Param("userId") Long userId,
                      @Param("deckId") Long deckId,
                      @Param("now") LocalDateTime now);

    /**
     * 查询用户在某卡组已学习的卡片ID列表
     */
    List<Long> selectLearnedCardIds(@Param("userId") Long userId,
                                    @Param("deckId") Long deckId);

    /**
     * 插入学习记录
     */
    int insert(FlashcardStudyRecord record);

    /**
     * 更新学习记录
     */
    int update(FlashcardStudyRecord record);

    /**
     * 统计用户在某卡组的掌握情况
     */
    int countByMasteryLevel(@Param("userId") Long userId,
                           @Param("deckId") Long deckId,
                           @Param("masteryLevel") int masteryLevel);

    /**
     * 统计用户已学习的卡片数
     */
    int countLearnedCards(@Param("userId") Long userId, @Param("deckId") Long deckId);
}
