package com.xiaou.community.mapper;

import com.xiaou.community.domain.CommunityTag;
import com.xiaou.community.dto.CommunityTagQueryRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 社区标签Mapper
 * 
 * @author xiaou
 */
@Mapper
public interface CommunityTagMapper {
    
    /**
     * 插入标签
     */
    int insert(CommunityTag tag);
    
    /**
     * 更新标签
     */
    int update(CommunityTag tag);
    
    /**
     * 根据ID删除标签
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 根据ID查询标签
     */
    CommunityTag selectById(@Param("id") Long id);
    
    /**
     * 根据名称查询标签
     */
    CommunityTag selectByName(@Param("name") String name);
    
    /**
     * 查询启用的标签列表
     */
    List<CommunityTag> selectEnabledList();
    
    /**
     * 查询热门标签列表
     */
    List<CommunityTag> selectHotList(@Param("limit") Integer limit);
    
    /**
     * 后台分页查询标签列表
     */
    List<CommunityTag> selectAdminList(CommunityTagQueryRequest request);
    
    /**
     * 更新标签帖子数量
     */
    int updatePostCount(@Param("tagId") Long tagId, @Param("increment") Integer increment);
}

