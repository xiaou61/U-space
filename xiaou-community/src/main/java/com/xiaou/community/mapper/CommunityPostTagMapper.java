package com.xiaou.community.mapper;

import com.xiaou.community.domain.CommunityPostTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 帖子标签关联Mapper
 * 
 * @author xiaou
 */
@Mapper
public interface CommunityPostTagMapper {
    
    /**
     * 批量插入帖子标签关联
     */
    int batchInsert(@Param("list") List<CommunityPostTag> list);
    
    /**
     * 根据帖子ID删除关联
     */
    int deleteByPostId(@Param("postId") Long postId);
    
    /**
     * 根据帖子ID查询标签ID列表
     */
    List<Long> selectTagIdsByPostId(@Param("postId") Long postId);
    
    /**
     * 根据标签ID查询帖子数量
     */
    int countPostsByTagId(@Param("tagId") Long tagId);
    
    /**
     * 查询用户的活跃标签（按使用次数排序）
     */
    List<Long> selectUserActiveTagIds(@Param("userId") Long userId, @Param("limit") Integer limit);
}

