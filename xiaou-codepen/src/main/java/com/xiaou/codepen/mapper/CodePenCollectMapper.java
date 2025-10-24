package com.xiaou.codepen.mapper;

import com.xiaou.codepen.domain.CodePenCollect;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 作品收藏Mapper
 * 
 * @author xiaou
 */
@Mapper
public interface CodePenCollectMapper {
    
    /**
     * 插入收藏记录
     */
    int insert(CodePenCollect collect);
    
    /**
     * 删除收藏记录
     */
    int delete(@Param("penId") Long penId, @Param("userId") Long userId);
    
    /**
     * 查询用户是否已收藏
     */
    CodePenCollect selectByPenIdAndUserId(@Param("penId") Long penId, @Param("userId") Long userId);
    
    /**
     * 查询用户的收藏列表
     */
    List<CodePenCollect> selectByUserId(@Param("userId") Long userId, @Param("folderId") Long folderId);
    
    /**
     * 查询作品的收藏数
     */
    Long selectCountByPenId(Long penId);
    
    /**
     * 查询收藏夹中的作品ID列表
     */
    List<Long> selectPenIdsByFolderId(@Param("folderId") Long folderId, @Param("userId") Long userId);
}

