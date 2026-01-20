package com.xiaou.flashcard.mapper;

import com.xiaou.flashcard.domain.FlashcardDeck;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 闪卡卡组 Mapper 接口
 *
 * @author xiaou
 */
@Mapper
public interface FlashcardDeckMapper {

    /**
     * 根据ID查询卡组
     */
    FlashcardDeck selectById(@Param("id") Long id);

    /**
     * 查询用户的卡组列表
     */
    List<FlashcardDeck> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询公开卡组列表
     */
    List<FlashcardDeck> selectPublicDecks(@Param("keyword") String keyword,
                                          @Param("tags") String tags);

    /**
     * 插入卡组
     */
    int insert(FlashcardDeck deck);

    /**
     * 更新卡组
     */
    int update(FlashcardDeck deck);

    /**
     * 删除卡组（逻辑删除）
     */
    int deleteById(@Param("id") Long id);

    /**
     * 更新卡片数量
     */
    int updateCardCount(@Param("id") Long id, @Param("delta") int delta);

    /**
     * 更新学习人数
     */
    int incrementStudyCount(@Param("id") Long id);

    /**
     * 更新复制次数
     */
    int incrementForkCount(@Param("id") Long id);
}
