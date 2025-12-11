package com.xiaou.flashcard.mapper;

import com.xiaou.flashcard.domain.FlashcardStudyRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 闪卡学习记录Mapper
 *
 * @author xiaou
 */
@Mapper
public interface FlashcardStudyRecordMapper {

    /**
     * 插入学习记录
     */
    int insert(FlashcardStudyRecord record);

    /**
     * 更新学习记录
     */
    int updateById(FlashcardStudyRecord record);

    /**
     * 根据用户ID和闪卡ID查询学习记录
     */
    FlashcardStudyRecord selectByUserAndCard(@Param("userId") Long userId, @Param("cardId") Long cardId);

    /**
     * 查询用户待复习的闪卡记录
     */
    List<FlashcardStudyRecord> selectDueReviews(@Param("userId") Long userId, 
                                                  @Param("deckId") Long deckId,
                                                  @Param("date") LocalDate date,
                                                  @Param("limit") Integer limit);

    /**
     * 统计用户待复习数量
     */
    int countDueReviews(@Param("userId") Long userId, @Param("date") LocalDate date);

    /**
     * 统计用户已掌握数量
     */
    int countMastered(@Param("userId") Long userId);

    /**
     * 统计用户学习中数量
     */
    int countLearning(@Param("userId") Long userId);

    /**
     * 统计用户某卡组的学习状态数量
     */
    int countByDeckAndStatus(@Param("userId") Long userId, @Param("deckId") Long deckId, @Param("status") Integer status);

    /**
     * 查询用户错误次数最多的闪卡记录
     */
    List<FlashcardStudyRecord> selectWeakCards(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * 统计用户总复习次数
     */
    int sumTotalReviews(@Param("userId") Long userId);

    /**
     * 统计某卡组待复习数量
     */
    int countDueReviewsByDeck(@Param("userId") Long userId, @Param("deckId") Long deckId, @Param("date") LocalDate date);

    /**
     * 统计某卡组已学习的卡片数量
     */
    int countLearnedByDeck(@Param("userId") Long userId, @Param("deckId") Long deckId);
}
