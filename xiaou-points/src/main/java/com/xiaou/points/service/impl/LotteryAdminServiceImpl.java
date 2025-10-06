package com.xiaou.points.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.exception.BusinessException;
import com.xiaou.common.utils.PageHelper;
import com.xiaou.points.domain.*;
import com.xiaou.points.dto.lottery.*;
import com.xiaou.points.dto.lottery.admin.*;
import com.xiaou.points.mapper.*;
import com.xiaou.points.service.LotteryAdminService;
import com.xiaou.points.service.LotteryNormalizeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 抽奖管理服务实现
 * 
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LotteryAdminServiceImpl implements LotteryAdminService {
    
    private final LotteryPrizeConfigMapper prizeConfigMapper;
    private final LotteryDrawRecordMapper drawRecordMapper;
    private final LotteryStatisticsDailyMapper statisticsMapper;
    private final LotteryAdjustHistoryMapper adjustHistoryMapper;
    private final UserLotteryLimitMapper userLimitMapper;
    private final LotteryNormalizeService normalizeService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void savePrizeConfig(PrizeConfigSaveRequest request, Long adminId) {
        LotteryPrizeConfig config = new LotteryPrizeConfig();
        BeanUtil.copyProperties(request, config);
        
        // 设置初始当前概率等于基础概率
        if (config.getCurrentProbability() == null) {
            config.setCurrentProbability(config.getBaseProbability());
        }
        
        if (config.getId() == null) {
            // 新增
            prizeConfigMapper.insert(config);
            log.info("管理员{}新增奖品配置：{}", adminId, config.getPrizeName());
        } else {
            // 更新
            prizeConfigMapper.updateById(config);
            log.info("管理员{}更新奖品配置：{}", adminId, config.getPrizeName());
        }
    }
    
    @Override
    public List<LotteryPrizeConfig> getPrizeConfigList() {
        return prizeConfigMapper.selectAll();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void togglePrizeStatus(Long prizeId, Boolean isActive, Long adminId) {
        LotteryPrizeConfig config = prizeConfigMapper.selectById(prizeId);
        if (config == null) {
            throw new BusinessException("奖品不存在");
        }
        
        config.setIsActive(isActive ? 1 : 0);
        prizeConfigMapper.updateById(config);
        
        log.info("管理员{}{}奖品：{}", adminId, isActive ? "启用" : "禁用", config.getPrizeName());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void suspendPrize(Long prizeId, Long suspendMinutes, Long adminId) {
        LotteryPrizeConfig config = prizeConfigMapper.selectById(prizeId);
        if (config == null) {
            throw new BusinessException("奖品不存在");
        }
        
        if (suspendMinutes == null || suspendMinutes <= 0) {
            // 恢复奖品
            prizeConfigMapper.resumePrize(prizeId);
            log.info("管理员{}恢复奖品：{}", adminId, config.getPrizeName());
        } else {
            // 暂停奖品
            LocalDateTime suspendUntil = LocalDateTime.now().plusMinutes(suspendMinutes);
            prizeConfigMapper.suspendPrize(prizeId, "管理员手动暂停", suspendUntil.toString());
            log.info("管理员{}暂停奖品：{}，暂停至：{}", adminId, config.getPrizeName(), suspendUntil);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adjustProbability(AdjustProbabilityRequest request, Long adminId) {
        LotteryPrizeConfig config = prizeConfigMapper.selectById(request.getPrizeId());
        if (config == null) {
            throw new BusinessException("奖品不存在");
        }
        
        BigDecimal oldProbability = config.getCurrentProbability();
        BigDecimal newProbability = request.getNewProbability();
        
        // 更新概率
        prizeConfigMapper.updateCurrentProbability(request.getPrizeId(), newProbability);
        
        // 记录调整历史
        LotteryAdjustHistory history = new LotteryAdjustHistory();
        history.setPrizeId(request.getPrizeId());
        history.setAdjustType("MANUAL");
        history.setOldProbability(oldProbability);
        history.setNewProbability(newProbability);
        history.setOldReturnRate(config.getActualReturnRate());
        history.setNewReturnRate(config.getActualReturnRate());
        history.setAdjustReason(request.getReason() != null ? request.getReason() : "管理员手动调整");
        history.setOperator("ADMIN");
        history.setOperatorId(adminId);
        history.setCreateTime(LocalDateTime.now());
        adjustHistoryMapper.insert(history);
        
        log.info("管理员{}调整奖品{}概率：{} -> {}", adminId, config.getPrizeName(), oldProbability, newProbability);
    }
    
    @Override
    public RealtimeMonitorResponse getRealtimeMonitor() {
        // 获取今日统计
        LotteryStatisticsDaily todayStats = statisticsMapper.selectToday();
        if (todayStats == null) {
            todayStats = createTodayStatistics();
        }
        
        // 构建今日概览
        RealtimeMonitorResponse.TodayOverview todayOverview = RealtimeMonitorResponse.TodayOverview.builder()
                .totalDrawCount(todayStats.getTotalDrawCount())
                .totalCostPoints(todayStats.getTotalCostPoints())
                .totalRewardPoints(todayStats.getTotalRewardPoints())
                .actualReturnRate(todayStats.getActualReturnRate())
                .profitPoints(todayStats.getPlatformProfitPoints())
                .profitRate(calculateProfitRate(todayStats))
                .uniqueUserCount(todayStats.getUniqueUserCount())
                .build();
        
        // 构建系统状态
        RealtimeMonitorResponse.SystemStatus systemStatus = RealtimeMonitorResponse.SystemStatus.builder()
                .status("运行中")
                .qps(0)
                .avgResponseTime(0)
                .successRate(BigDecimal.valueOf(0.999))
                .activeUsers(todayStats.getUniqueUserCount())
                .build();
        
        // 获取所有奖品状态
        List<LotteryPrizeConfig> prizes = prizeConfigMapper.selectAll();
        List<RealtimeMonitorResponse.PrizeStatus> prizeStatusList = prizes.stream()
                .map(this::convertToPrizeStatus)
                .collect(Collectors.toList());
        
        // 构建策略信息
        RealtimeMonitorResponse.StrategyInfo strategyInfo = RealtimeMonitorResponse.StrategyInfo.builder()
                .currentStrategy("Alias Method")
                .autoAdjustEnabled(true)
                .build();
        
        return RealtimeMonitorResponse.builder()
                .systemStatus(systemStatus)
                .todayOverview(todayOverview)
                .prizeStatusList(prizeStatusList)
                .strategyInfo(strategyInfo)
                .build();
    }
    
    /**
     * 计算利润率
     */
    private BigDecimal calculateProfitRate(LotteryStatisticsDaily stats) {
        if (stats.getTotalCostPoints() == null || stats.getTotalCostPoints() == 0) {
            return BigDecimal.ZERO;
        }
        Long profit = stats.getPlatformProfitPoints() != null ? stats.getPlatformProfitPoints() : 0L;
        return BigDecimal.valueOf(profit)
                .divide(BigDecimal.valueOf(stats.getTotalCostPoints()), 4, RoundingMode.HALF_UP);
    }
    
    /**
     * 转换为奖品状态
     */
    private RealtimeMonitorResponse.PrizeStatus convertToPrizeStatus(LotteryPrizeConfig config) {
        String status = "正常";
        String alertLevel = "无";
        String alertMessage = null;
        
        if (config.getIsSuspended() != null && config.getIsSuspended() == 1) {
            status = "暂停";
            alertLevel = "警告";
            alertMessage = "奖品已暂停";
        } else if (config.getActualReturnRate() != null && 
                   config.getMaxReturnRate() != null && 
                   config.getActualReturnRate().compareTo(config.getMaxReturnRate()) > 0) {
            alertLevel = "危险";
            alertMessage = "回报率超标";
        }
        
        return RealtimeMonitorResponse.PrizeStatus.builder()
                .prizeId(config.getId())
                .prizeName(config.getPrizeName())
                .currentProbability(config.getCurrentProbability())
                .actualReturnRate(config.getActualReturnRate())
                .status(status)
                .todayWinCount(config.getTodayWinCount())
                .alertLevel(alertLevel)
                .alertMessage(alertMessage)
                .build();
    }
    
    @Override
    public PrizeMonitorResponse getPrizeMonitor(Long prizeId) {
        LotteryPrizeConfig config = prizeConfigMapper.selectById(prizeId);
        if (config == null) {
            throw new BusinessException("奖品不存在");
        }
        
        return convertToPrizeMonitor(config);
    }
    
    @Override
    public PageResult<LotteryDrawResponse> getAllDrawRecords(LotteryRecordQueryRequest request) {
        return PageHelper.doPage(request.getPage(), request.getSize(), () -> {
            List<LotteryDrawRecord> records = drawRecordMapper.selectAll(request);
            return records.stream()
                    .map(this::convertToDrawResponse)
                    .collect(Collectors.toList());
        });
    }
    
    @Override
    public List<LotteryStatisticsResponse> getHistoryStatistics(String startDate, String endDate) {
        LocalDate start = LocalDate.parse(startDate, DateTimeFormatter.ISO_DATE);
        LocalDate end = LocalDate.parse(endDate, DateTimeFormatter.ISO_DATE);
        
        List<LotteryStatisticsDaily> dailyStats = statisticsMapper.selectByDateRange(start, end);
        
        return dailyStats.stream()
                .map(this::convertToStatisticsResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public PageResult<AdjustHistoryResponse> getAdjustHistory(Long prizeId, Integer page, Integer size) {
        return PageHelper.doPage(page, size, () -> {
            List<LotteryAdjustHistory> histories;
            if (prizeId != null) {
                histories = adjustHistoryMapper.selectByPrizeId(prizeId);
            } else {
                histories = adjustHistoryMapper.selectAll();
            }
            return histories.stream()
                    .map(this::convertToAdjustHistoryResponse)
                    .collect(Collectors.toList());
        });
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetUserLimit(Long userId, Long adminId) {
        UserLotteryLimit limit = userLimitMapper.selectByUserId(userId);
        if (limit == null) {
            throw new BusinessException("用户限制记录不存在");
        }
        
        limit.setTodayDrawCount(0);
        limit.setWeekDrawCount(0);
        limit.setMonthDrawCount(0);
        limit.setTodayWinCount(0);
        limit.setCurrentContinuousNoWin(0);
        userLimitMapper.updateById(limit);
        
        log.info("管理员{}重置用户{}的抽奖限制", adminId, userId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setUserBlacklist(Long userId, Boolean isBlacklist, Long adminId) {
        UserLotteryLimit limit = userLimitMapper.selectByUserId(userId);
        if (limit == null) {
            throw new BusinessException("用户限制记录不存在");
        }
        
        limit.setIsBlacklist(isBlacklist ? 1 : 0);
        userLimitMapper.updateById(limit);
        
        log.info("管理员{}{}用户{}黑名单", adminId, isBlacklist ? "加入" : "移除", userId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchAdjustProbability(BatchAdjustProbabilityRequest request, Long adminId) {
        if (request.getPrizes() == null || request.getPrizes().isEmpty()) {
            throw new BusinessException("奖品列表不能为空");
        }
        
        log.info("管理员{}批量调整概率，奖品数量：{}", adminId, request.getPrizes().size());
        
        // 批量调整概率
        for (BatchAdjustProbabilityRequest.PrizeAdjustItem item : request.getPrizes()) {
            LotteryPrizeConfig config = prizeConfigMapper.selectById(item.getPrizeId());
            if (config == null) {
                log.warn("奖品{}不存在，跳过", item.getPrizeId());
                continue;
            }
            
            BigDecimal oldProbability = config.getCurrentProbability();
            BigDecimal newProbability = item.getNewProbability();
            
            // 更新概率
            prizeConfigMapper.updateCurrentProbability(item.getPrizeId(), newProbability);
            
            // 记录调整历史
            LotteryAdjustHistory history = new LotteryAdjustHistory();
            history.setPrizeId(item.getPrizeId());
            history.setAdjustType("MANUAL");
            history.setOldProbability(oldProbability);
            history.setNewProbability(newProbability);
            history.setOldReturnRate(config.getActualReturnRate());
            history.setNewReturnRate(config.getActualReturnRate());
            history.setAdjustReason(request.getReason() != null ? request.getReason() : "管理员批量调整");
            history.setOperator("ADMIN");
            history.setOperatorId(adminId);
            history.setCreateTime(LocalDateTime.now());
            adjustHistoryMapper.insert(history);
            
            log.info("奖品{}概率调整：{} -> {}", config.getPrizeName(), oldProbability, newProbability);
        }
        
        // 如果需要自动归一化
        if (request.getAutoNormalize() != null && request.getAutoNormalize()) {
            BigDecimal sum = normalizeService.normalizeAllProbabilities();
            log.info("批量调整后自动归一化，概率总和：{}", sum);
        }
        
        log.info("管理员{}批量调整概率完成", adminId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchToggleStatus(BatchToggleStatusRequest request, Long adminId) {
        if (request.getPrizeIds() == null || request.getPrizeIds().isEmpty()) {
            throw new BusinessException("奖品ID列表不能为空");
        }
        
        log.info("管理员{}批量{}奖品，数量：{}", adminId, request.getIsActive() ? "启用" : "禁用", request.getPrizeIds().size());
        
        int successCount = 0;
        for (Long prizeId : request.getPrizeIds()) {
            LotteryPrizeConfig config = prizeConfigMapper.selectById(prizeId);
            if (config == null) {
                log.warn("奖品{}不存在，跳过", prizeId);
                continue;
            }
            
            config.setIsActive(request.getIsActive() ? 1 : 0);
            prizeConfigMapper.updateById(config);
            successCount++;
            
            log.info("{}奖品：{}", request.getIsActive() ? "启用" : "禁用", config.getPrizeName());
        }
        
        log.info("管理员{}批量{}奖品完成，成功{}个", adminId, request.getIsActive() ? "启用" : "禁用", successCount);
    }
    
    @Override
    public List<AlertInfo> getAlerts() {
        List<AlertInfo> alerts = new java.util.ArrayList<>();
        
        // 检查所有奖品的回报率预警
        List<LotteryPrizeConfig> prizes = prizeConfigMapper.selectAll();
        for (LotteryPrizeConfig prize : prizes) {
            // 检查回报率是否超标
            if (prize.getActualReturnRate() != null && prize.getMaxReturnRate() != null) {
                if (prize.getActualReturnRate().compareTo(prize.getMaxReturnRate()) > 0) {
                    alerts.add(AlertInfo.builder()
                            .alertTime(LocalDateTime.now())
                            .alertLevel("HIGH")
                            .alertType("RETURN_RATE_EXCEED")
                            .message("奖品【" + prize.getPrizeName() + "】回报率" + prize.getActualReturnRate() + "超过阈值" + prize.getMaxReturnRate())
                            .handled(prize.getIsSuspended() != null && prize.getIsSuspended() == 1)
                            .data(prize)
                            .build());
                }
            }
            
            // 检查库存预警
            if (prize.getCurrentStock() != null && prize.getCurrentStock() > 0 && prize.getCurrentStock() < 10) {
                alerts.add(AlertInfo.builder()
                        .alertTime(LocalDateTime.now())
                        .alertLevel("MEDIUM")
                        .alertType("STOCK_LOW")
                        .message("奖品【" + prize.getPrizeName() + "】库存不足，仅剩" + prize.getCurrentStock() + "个")
                        .handled(false)
                        .data(prize)
                        .build());
            }
        }
        
        return alerts;
    }
    
    /**
     * 创建今日统计
     */
    private LotteryStatisticsDaily createTodayStatistics() {
        LotteryStatisticsDaily stats = new LotteryStatisticsDaily();
        stats.setStatDate(LocalDate.now());
        stats.setTotalDrawCount(0);
        stats.setTotalCostPoints(0L);
        stats.setTotalRewardPoints(0L);
        stats.setPlatformProfitPoints(0L);
        stats.setActualReturnRate(BigDecimal.ZERO);
        stats.setUniqueUserCount(0);
        stats.setAvgDrawPerUser(BigDecimal.ZERO);
        statisticsMapper.insert(stats);
        return stats;
    }
    
    /**
     * 转换为奖品监控响应
     */
    private PrizeMonitorResponse convertToPrizeMonitor(LotteryPrizeConfig config) {
        PrizeMonitorResponse response = new PrizeMonitorResponse();
        response.setPrizeId(config.getId());
        response.setPrizeName(config.getPrizeName());
        response.setPrizePoints(config.getPrizePoints());
        response.setBaseProbability(config.getBaseProbability());
        response.setCurrentProbability(config.getCurrentProbability());
        response.setTargetReturnRate(config.getTargetReturnRate());
        response.setActualReturnRate(config.getActualReturnRate());
        response.setTodayDrawCount(config.getTodayDrawCount());
        response.setTodayWinCount(config.getTodayWinCount());
        response.setTotalDrawCount(config.getTotalDrawCount());
        response.setTotalWinCount(config.getTotalWinCount());
        response.setTodayCost((long) config.getTodayWinCount() * config.getPrizePoints());
        response.setTotalCost((long) config.getTotalWinCount() * config.getPrizePoints());
        response.setIsSuspended(config.getIsSuspended() != null && config.getIsSuspended() == 1);
        response.setSuspendUntil(config.getSuspendUntil());
        response.setAdjustStrategy(config.getAdjustStrategy());
        response.setLastAdjustTime(config.getLastAdjustTime());
        return response;
    }
    
    /**
     * 转换为抽奖响应
     */
    private LotteryDrawResponse convertToDrawResponse(LotteryDrawRecord record) {
        LotteryDrawResponse response = new LotteryDrawResponse();
        BeanUtil.copyProperties(record, response);
        response.setRecordId(record.getId());
        response.setDrawTime(record.getCreateTime());
        
        LotteryPrizeConfig prize = prizeConfigMapper.selectById(record.getPrizeId());
        if (prize != null) {
            response.setPrizeName(prize.getPrizeName());
            response.setPrizeIcon(prize.getPrizeIcon());
        }
        
        return response;
    }
    
    /**
     * 转换为统计响应
     */
    private LotteryStatisticsResponse convertToStatisticsResponse(LotteryStatisticsDaily daily) {
        LotteryStatisticsResponse response = new LotteryStatisticsResponse();
        response.setTotalDrawCount(daily.getTotalDrawCount());
        response.setTotalWinCount(daily.getTotalWinCount());
        return response;
    }
    
    /**
     * 转换为调整历史响应
     */
    private AdjustHistoryResponse convertToAdjustHistoryResponse(LotteryAdjustHistory history) {
        AdjustHistoryResponse response = new AdjustHistoryResponse();
        BeanUtil.copyProperties(history, response);
        response.setReason(history.getAdjustReason());
        
        // 获取奖品名称
        LotteryPrizeConfig prize = prizeConfigMapper.selectById(history.getPrizeId());
        if (prize != null) {
            response.setPrizeName(prize.getPrizeName());
        }
        
        // 设置操作人姓名
        if (history.getOperatorId() == 0) {
            response.setOperatorName("系统自动");
        } else {
            response.setOperatorName("管理员" + history.getOperatorId());
        }
        
        return response;
    }
}

