package com.xiaou.points.service;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.points.domain.LotteryPrizeConfig;
import com.xiaou.points.dto.lottery.*;
import com.xiaou.points.dto.lottery.admin.*;

import java.util.List;

/**
 * 抽奖管理服务接口
 * 
 * @author xiaou
 */
public interface LotteryAdminService {
    
    /**
     * 保存奖品配置
     */
    void savePrizeConfig(PrizeConfigSaveRequest request, Long adminId);
    
    /**
     * 获取奖品配置列表（管理端）
     */
    List<LotteryPrizeConfig> getPrizeConfigList();
    
    /**
     * 启用/禁用奖品
     */
    void togglePrizeStatus(Long prizeId, Boolean isActive, Long adminId);
    
    /**
     * 暂停/恢复奖品
     */
    void suspendPrize(Long prizeId, Long suspendMinutes, Long adminId);
    
    /**
     * 手动调整概率
     */
    void adjustProbability(AdjustProbabilityRequest request, Long adminId);
    
    /**
     * 获取实时监控数据
     */
    RealtimeMonitorResponse getRealtimeMonitor();
    
    /**
     * 获取单个奖品监控数据
     */
    PrizeMonitorResponse getPrizeMonitor(Long prizeId);
    
    /**
     * 获取所有抽奖记录（管理端）
     */
    PageResult<LotteryDrawResponse> getAllDrawRecords(LotteryRecordQueryRequest request);
    
    /**
     * 获取抽奖历史统计
     */
    List<LotteryStatisticsResponse> getHistoryStatistics(String startDate, String endDate);
    
    /**
     * 获取概率调整历史
     */
    PageResult<AdjustHistoryResponse> getAdjustHistory(Long prizeId, Integer page, Integer size);
    
    /**
     * 重置用户抽奖限制
     */
    void resetUserLimit(Long userId, Long adminId);
    
    /**
     * 设置用户黑名单
     */
    void setUserBlacklist(Long userId, Boolean isBlacklist, Long adminId);
    
    /**
     * 批量调整概率
     */
    void batchAdjustProbability(BatchAdjustProbabilityRequest request, Long adminId);
    
    /**
     * 批量启用/禁用奖品
     */
    void batchToggleStatus(BatchToggleStatusRequest request, Long adminId);
    
    /**
     * 获取预警信息列表
     */
    List<AlertInfo> getAlerts();
}

