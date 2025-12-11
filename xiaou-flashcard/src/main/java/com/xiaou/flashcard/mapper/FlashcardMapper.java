package com.xiaou.flashcard.mapper;

import com.xiaou.flashcard.domain.Flashcard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 闪卡Mapper
 *
 * @author xiaou
 */
@Mapper
public interface FlashcardMapper {

    /**
     * 插入闪卡
     */
    int insert(Flashcard flashcard);

    /**
     * 批量插入闪卡
     */
    int batchInsert(@Param("flashcards") List<Flashcard> flashcards);

    /**
     * 根据ID删除闪卡
     */
    int deleteById(Long id);

    /**
     * 根据卡组ID删除闪卡
     */
    int deleteByDeckId(@Param("deckId") Long deckId);

    /**
     * 更新闪卡
     */
    int updateById(Flashcard flashcard);

    /**
     * 根据ID查询闪卡
     */
    Flashcard selectById(Long id);

    /**
     * 根据卡组ID查询闪卡列表
     */
    List<Flashcard> selectByDeckId(@Param("deckId") Long deckId);

    /**
     * 根据用户ID查询闪卡列表
     */
    List<Flashcard> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据面试题ID查询闪卡
     */
    Flashcard selectByQuestionId(@Param("questionId") Long questionId, @Param("userId") Long userId);

    /**
     * 统计用户闪卡总数
     */
    int countByUserId(@Param("userId") Long userId);

    /**
     * 统计卡组闪卡数量
     */
    int countByDeckId(@Param("deckId") Long deckId);

    /**
     * 查询用户没有学习记录的新卡
     */
    List<Flashcard> selectNewCards(@Param("userId") Long userId, @Param("deckId") Long deckId, @Param("limit") Integer limit);
}
