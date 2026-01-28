package com.xiaou.flashcard.mapper;

import com.xiaou.flashcard.domain.Flashcard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 闪卡 Mapper 接口
 *
 * @author xiaou
 */
@Mapper
public interface FlashcardMapper {

    /**
     * 根据ID查询闪卡
     */
    Flashcard selectById(@Param("id") Long id);

    /**
     * 查询卡组内的闪卡列表
     */
    List<Flashcard> selectByDeckId(@Param("deckId") Long deckId);

    /**
     * 查询卡组内的闪卡ID列表
     */
    List<Long> selectIdsByDeckId(@Param("deckId") Long deckId);

    /**
     * 插入闪卡
     */
    int insert(Flashcard flashcard);

    /**
     * 批量插入闪卡
     */
    int batchInsert(@Param("list") List<Flashcard> flashcards);

    /**
     * 更新闪卡
     */
    int update(Flashcard flashcard);

    /**
     * 删除闪卡（逻辑删除）
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据卡组ID删除闪卡（逻辑删除）
     */
    int deleteByDeckId(@Param("deckId") Long deckId);

    /**
     * 统计卡组内的闪卡数量
     */
    int countByDeckId(@Param("deckId") Long deckId);

    /**
     * 检查题目是否已导入
     */
    int countBySourceQuestionId(@Param("deckId") Long deckId,
                                @Param("sourceQuestionId") Long sourceQuestionId);
}
