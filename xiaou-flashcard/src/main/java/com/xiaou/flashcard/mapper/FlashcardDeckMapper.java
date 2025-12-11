package com.xiaou.flashcard.mapper;

import com.xiaou.flashcard.domain.FlashcardDeck;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 闪卡卡组Mapper
 *
 * @author xiaou
 */
@Mapper
public interface FlashcardDeckMapper {

    /**
     * 插入卡组
     */
    int insert(FlashcardDeck deck);

    /**
     * 根据ID删除卡组
     */
    int deleteById(Long id);

    /**
     * 更新卡组
     */
    int updateById(FlashcardDeck deck);

    /**
     * 根据ID查询卡组
     */
    FlashcardDeck selectById(Long id);

    /**
     * 查询用户的卡组列表
     */
    List<FlashcardDeck> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询官方卡组列表
     */
    List<FlashcardDeck> selectOfficialDecks();

    /**
     * 查询公开卡组列表
     */
    List<FlashcardDeck> selectPublicDecks();

    /**
     * 更新卡组的闪卡数量
     */
    int updateCardCount(@Param("deckId") Long deckId, @Param("count") Integer count);

    /**
     * 统计卡组中的闪卡数量
     */
    int countCardsByDeckId(@Param("deckId") Long deckId);
}
