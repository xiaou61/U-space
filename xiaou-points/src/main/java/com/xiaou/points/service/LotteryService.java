package com.xiaou.points.service;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.points.dto.lottery.*;

import java.util.List;

/**
 * 抽奖服务接口
 * 
 * @author xiaou
 */
public interface LotteryService {
    
    /**
     * 执行抽奖
     */
    LotteryDrawResponse draw(LotteryDrawRequest request, Long userId, String ip, String device);
    
    /**
     * 获取奖品配置列表
     */
    List<LotteryPrizeResponse> getPrizeList();
    
    /**
     * 获取用户抽奖记录
     */
    PageResult<LotteryDrawResponse> getUserDrawRecords(LotteryRecordQueryRequest request, Long userId);
    
    /**
     * 获取用户抽奖统计
     */
    LotteryStatisticsResponse getUserStatistics(Long userId);
    
    /**
     * 获取抽奖规则说明
     */
    String getLotteryRules();
    
    /**
     * 获取用户今日剩余抽奖次数
     */
    Integer getTodayRemainingCount(Long userId);
}

