package com.xiaou.community.mapper;

import com.xiaou.community.domain.CommunityPostLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 帖子点赞Mapper
 * 
 * @author xiaou
 */
@Mapper
public interface CommunityPostLikeMapper {
    
    /**
     * 插入点赞记录
     */
    int insert(CommunityPostLike like);
    
    /**
     * 删除点赞记录
     */
    int delete(@Param("postId") Long postId, @Param("userId") Long userId);
    
    /**
     * 查询是否已点赞
     */
    CommunityPostLike selectByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);
    
    /**
     * 统计帖子点赞数
     */
    int countByPostId(Long postId);
} 