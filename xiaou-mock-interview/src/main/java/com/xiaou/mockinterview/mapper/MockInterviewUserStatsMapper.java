package com.xiaou.mockinterview.mapper;

import com.xiaou.mockinterview.domain.MockInterviewUserStats;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户面试统计Mapper
 *
 * @author xiaou
 */
@Mapper
public interface MockInterviewUserStatsMapper {

    /**
     * 插入用户统计记录
     */
    int insert(MockInterviewUserStats stats);

    /**
     * 根据用户ID更新统计记录
     */
    int updateByUserId(MockInterviewUserStats stats);

    /**
     * 根据用户ID查询统计记录
     */
    MockInterviewUserStats selectByUserId(Long userId);

    /**
     * 根据用户ID删除统计记录
     */
    int deleteByUserId(Long userId);

    /**
     * 增加面试次数
     */
    int incrementInterviewCount(Long userId);

    /**
     * 增加完成面试次数
     */
    int incrementCompletedCount(Long userId);
}
