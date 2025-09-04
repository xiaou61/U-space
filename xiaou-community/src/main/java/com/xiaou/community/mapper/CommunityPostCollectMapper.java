package com.xiaou.community.mapper;

import com.xiaou.community.domain.CommunityPostCollect;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 帖子收藏Mapper
 * 
 * @author xiaou
 */
@Mapper
public interface CommunityPostCollectMapper {
    
    /**
     * 插入收藏记录
     */
    int insert(CommunityPostCollect collect);
    
    /**
     * 删除收藏记录
     */
    int delete(@Param("postId") Long postId, @Param("userId") Long userId);
    
    /**
     * 查询是否已收藏
     */
    CommunityPostCollect selectByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);
    
    /**
     * 统计帖子收藏数
     */
    int countByPostId(Long postId);
    
    /**
     * 查询用户收藏列表
     */
    List<CommunityPostCollect> selectByUserId(Long userId);
} 