package com.xiaou.community.mapper;

import com.xiaou.community.domain.CommunityCommentLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 评论点赞Mapper
 * 
 * @author xiaou
 */
@Mapper
public interface CommunityCommentLikeMapper {
    
    /**
     * 插入点赞记录
     */
    int insert(CommunityCommentLike like);
    
    /**
     * 删除点赞记录
     */
    int delete(@Param("commentId") Long commentId, @Param("userId") Long userId);
    
    /**
     * 查询是否已点赞
     */
    CommunityCommentLike selectByCommentIdAndUserId(@Param("commentId") Long commentId, @Param("userId") Long userId);
    
    /**
     * 统计评论点赞数
     */
    int countByCommentId(Long commentId);
} 