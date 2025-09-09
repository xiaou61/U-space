package com.xiaou.moment.mapper;

import com.xiaou.moment.domain.MomentComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 动态评论Mapper接口
 */
@Mapper
public interface MomentCommentMapper {
    
    /**
     * 插入评论
     */
    int insert(MomentComment momentComment);
    
    /**
     * 根据ID删除评论
     */
    int deleteById(Long id);
    
    /**
     * 根据ID更新评论
     */
    int updateById(MomentComment momentComment);
    
    /**
     * 根据ID查询评论
     */
    MomentComment selectById(Long id);
    
    /**
     * 根据动态ID查询评论列表
     */
    List<MomentComment> selectByMomentId(@Param("momentId") Long momentId, @Param("limit") Integer limit);
    
    /**
     * 查询评论列表
     */
    List<MomentComment> selectList(Map<String, Object> params);
    
    /**
     * 查询评论总数
     */
    Long selectCount(Map<String, Object> params);
    
    /**
     * 根据动态ID删除所有评论
     */
    int deleteByMomentId(Long momentId);
} 