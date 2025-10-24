package com.xiaou.codepen.mapper;

import com.xiaou.codepen.domain.CodePenLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 作品点赞Mapper
 * 
 * @author xiaou
 */
@Mapper
public interface CodePenLikeMapper {
    
    /**
     * 插入点赞记录
     */
    int insert(CodePenLike like);
    
    /**
     * 删除点赞记录
     */
    int delete(@Param("penId") Long penId, @Param("userId") Long userId);
    
    /**
     * 查询用户是否已点赞
     */
    CodePenLike selectByPenIdAndUserId(@Param("penId") Long penId, @Param("userId") Long userId);
    
    /**
     * 查询作品的点赞数
     */
    Long selectCountByPenId(Long penId);
}

