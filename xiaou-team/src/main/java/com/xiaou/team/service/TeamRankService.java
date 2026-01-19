package com.xiaou.team.service;

import com.xiaou.team.dto.RankResponse;

import java.util.List;

/**
 * 排行榜服务接口
 * 
 * @author xiaou
 */
public interface TeamRankService {
    
    /**
     * 获取打卡次数排行榜
     * 
     * @param teamId 小组ID
     * @param type 类型：weekly/monthly/total
     * @param limit 条数
     * @param userId 当前用户ID
     * @return 排行榜
     */
    List<RankResponse> getCheckinRank(Long teamId, String type, Integer limit, Long userId);
    
    /**
     * 获取连续打卡排行榜
     * 
     * @param teamId 小组ID
     * @param limit 条数
     * @param userId 当前用户ID
     * @return 排行榜
     */
    List<RankResponse> getStreakRank(Long teamId, Integer limit, Long userId);
    
    /**
     * 获取学习时长排行榜
     * 
     * @param teamId 小组ID
     * @param type 类型：weekly/monthly/total
     * @param limit 条数
     * @param userId 当前用户ID
     * @return 排行榜
     */
    List<RankResponse> getDurationRank(Long teamId, String type, Integer limit, Long userId);
    
    /**
     * 获取贡献值排行榜
     * 
     * @param teamId 小组ID
     * @param limit 条数
     * @param userId 当前用户ID
     * @return 排行榜
     */
    List<RankResponse> getContributionRank(Long teamId, Integer limit, Long userId);
    
    /**
     * 获取用户在排行榜中的排名
     * 
     * @param teamId 小组ID
     * @param userId 用户ID
     * @param rankType 排行类型：checkin/streak/duration/contribution
     * @return 排名信息
     */
    RankResponse getUserRank(Long teamId, Long userId, String rankType);
}
