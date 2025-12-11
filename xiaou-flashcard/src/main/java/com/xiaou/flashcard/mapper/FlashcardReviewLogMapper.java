package com.xiaou.flashcard.mapper;

import com.xiaou.flashcard.domain.FlashcardReviewLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 闪卡复习日志Mapper
 *
 * @author xiaou
 */
@Mapper
public interface FlashcardReviewLogMapper {

    /**
     * 插入复习日志
     */
    int insert(FlashcardReviewLog log);

    /**
     * 查询用户某闪卡的复习历史
     */
    List<FlashcardReviewLog> selectByUserAndCard(@Param("userId") Long userId, @Param("cardId") Long cardId);

    /**
     * 查询用户最近的复习记录
     */
    List<FlashcardReviewLog> selectRecentLogs(@Param("userId") Long userId, @Param("limit") Integer limit);
}
