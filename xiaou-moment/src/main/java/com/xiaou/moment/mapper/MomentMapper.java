package com.xiaou.moment.mapper;

import com.xiaou.moment.domain.Moment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 动态Mapper接口
 */
@Mapper
public interface MomentMapper {
    
    /**
     * 插入动态
     */
    int insert(Moment moment);
    
    /**
     * 根据ID删除动态
     */
    int deleteById(Long id);
    
    /**
     * 根据ID更新动态
     */
    int updateById(Moment moment);
    
    /**
     * 根据ID查询动态
     */
    Moment selectById(Long id);
    
    /**
     * 查询动态列表
     */
    List<Moment> selectList(Map<String, Object> params);
    
    /**
     * 查询动态总数
     */
    Long selectCount(Map<String, Object> params);
    
    /**
     * 增加点赞数
     */
    int incrementLikeCount(Long momentId);
    
    /**
     * 减少点赞数
     */
    int decrementLikeCount(Long momentId);
    
    /**
     * 增加评论数
     */
    int incrementCommentCount(Long momentId);
    
    /**
     * 减少评论数
     */
    int decrementCommentCount(Long momentId);
    
    /**
     * 统计总点赞数
     */
    Long selectTotalLikes(Map<String, Object> params);
    
    /**
     * 统计活跃用户数
     */
    Long selectActiveUsersCount(Map<String, Object> params);
} 