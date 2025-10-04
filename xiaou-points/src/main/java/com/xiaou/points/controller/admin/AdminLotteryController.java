package com.xiaou.points.controller.admin;

import com.xiaou.common.annotation.RequireAdmin;
import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.core.domain.Result;
import com.xiaou.common.satoken.StpAdminUtil;
import com.xiaou.points.domain.LotteryPrizeConfig;
import com.xiaou.points.dto.lottery.*;
import com.xiaou.points.dto.lottery.admin.*;
import com.xiaou.points.service.LotteryAdminService;
import com.xiaou.points.service.LotteryNormalizeService;
import com.xiaou.points.service.LotteryRiskService;
import com.xiaou.points.service.LotteryAnalysisService;
import com.xiaou.points.service.LotteryEmergencyService;
import com.xiaou.points.cache.LotteryCacheWarmer;
import com.xiaou.points.dto.lottery.RiskUserQueryRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * 管理端抽奖控制器
 * 
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/admin/lottery")
@RequiredArgsConstructor
public class AdminLotteryController {
    
    private final LotteryAdminService lotteryAdminService;
    private final LotteryNormalizeService normalizeService;
    private final LotteryRiskService riskService;
    private final LotteryAnalysisService analysisService;
    private final LotteryEmergencyService emergencyService;
    private final LotteryCacheWarmer cacheWarmer;
    
    /**
     * 保存奖品配置
     */
    @RequireAdmin(message = "需要管理员权限才能配置奖品")
    @PostMapping("/prize/save")
    public Result<String> savePrizeConfig(@Valid @RequestBody PrizeConfigSaveRequest request) {
        try {
            Long adminId = StpAdminUtil.getLoginIdAsLong();
            lotteryAdminService.savePrizeConfig(request, adminId);
            return Result.success("保存成功");
        } catch (Exception e) {
            log.error("保存奖品配置失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取奖品配置列表
     */
    @RequireAdmin(message = "需要管理员权限")
    @GetMapping("/prize/list")
    public Result<List<LotteryPrizeConfig>> getPrizeConfigList() {
        try {
            List<LotteryPrizeConfig> prizes = lotteryAdminService.getPrizeConfigList();
            return Result.success("获取成功", prizes);
        } catch (Exception e) {
            log.error("获取奖品配置列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 启用/禁用奖品
     */
    @RequireAdmin(message = "需要管理员权限")
    @PostMapping("/prize/toggle-status")
    public Result<String> togglePrizeStatus(@RequestParam Long prizeId, 
                                            @RequestParam Boolean isActive) {
        try {
            Long adminId = StpAdminUtil.getLoginIdAsLong();
            lotteryAdminService.togglePrizeStatus(prizeId, isActive, adminId);
            return Result.success(isActive ? "启用成功" : "禁用成功");
        } catch (Exception e) {
            log.error("切换奖品状态失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 暂停/恢复奖品
     */
    @RequireAdmin(message = "需要管理员权限")
    @PostMapping("/prize/suspend")
    public Result<String> suspendPrize(@RequestParam Long prizeId, 
                                       @RequestParam(required = false) Long suspendMinutes) {
        try {
            Long adminId = StpAdminUtil.getLoginIdAsLong();
            lotteryAdminService.suspendPrize(prizeId, suspendMinutes, adminId);
            return Result.success(suspendMinutes == null || suspendMinutes <= 0 ? "恢复成功" : "暂停成功");
        } catch (Exception e) {
            log.error("暂停/恢复奖品失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 手动调整概率
     */
    @RequireAdmin(message = "需要管理员权限")
    @PostMapping("/prize/adjust-probability")
    public Result<String> adjustProbability(@Valid @RequestBody AdjustProbabilityRequest request) {
        try {
            Long adminId = StpAdminUtil.getLoginIdAsLong();
            lotteryAdminService.adjustProbability(request, adminId);
            return Result.success("调整成功");
        } catch (Exception e) {
            log.error("调整概率失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取实时监控数据
     */
    @RequireAdmin(message = "需要管理员权限")
    @GetMapping("/monitor/realtime")
    public Result<RealtimeMonitorResponse> getRealtimeMonitor() {
        try {
            RealtimeMonitorResponse response = lotteryAdminService.getRealtimeMonitor();
            return Result.success("获取成功", response);
        } catch (Exception e) {
            log.error("获取实时监控数据失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取单个奖品监控数据
     */
    @RequireAdmin(message = "需要管理员权限")
    @GetMapping("/monitor/prize/{prizeId}")
    public Result<PrizeMonitorResponse> getPrizeMonitor(@PathVariable Long prizeId) {
        try {
            PrizeMonitorResponse response = lotteryAdminService.getPrizeMonitor(prizeId);
            return Result.success("获取成功", response);
        } catch (Exception e) {
            log.error("获取奖品监控数据失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取所有抽奖记录
     */
    @RequireAdmin(message = "需要管理员权限")
    @PostMapping("/records")
    public Result<PageResult<LotteryDrawResponse>> getAllDrawRecords(@RequestBody LotteryRecordQueryRequest request) {
        try {
            PageResult<LotteryDrawResponse> response = lotteryAdminService.getAllDrawRecords(request);
            return Result.success("获取成功", response);
        } catch (Exception e) {
            log.error("获取抽奖记录失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取历史统计数据
     */
    @RequireAdmin(message = "需要管理员权限")
    @PostMapping("/statistics/history")
    public Result<List<LotteryStatisticsResponse>> getHistoryStatistics(@RequestBody StatisticsQueryRequest request) {
        try {
            List<LotteryStatisticsResponse> response = lotteryAdminService.getHistoryStatistics(
                request.getStartDate(), 
                request.getEndDate()
            );
            return Result.success("获取成功", response);
        } catch (Exception e) {
            log.error("获取历史统计数据失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取概率调整历史
     */
    @RequireAdmin(message = "需要管理员权限")
    @GetMapping("/adjust-history")
    public Result<PageResult<AdjustHistoryResponse>> getAdjustHistory(
            @RequestParam(required = false) Long prizeId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        try {
            PageResult<AdjustHistoryResponse> response = lotteryAdminService.getAdjustHistory(prizeId, page, size);
            return Result.success("获取成功", response);
        } catch (Exception e) {
            log.error("获取调整历史失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 重置用户抽奖限制
     */
    @RequireAdmin(message = "需要管理员权限")
    @PostMapping("/user/reset-limit")
    public Result<String> resetUserLimit(@RequestParam Long userId) {
        try {
            Long adminId = StpAdminUtil.getLoginIdAsLong();
            lotteryAdminService.resetUserLimit(userId, adminId);
            return Result.success("重置成功");
        } catch (Exception e) {
            log.error("重置用户限制失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 设置用户黑名单
     */
    @RequireAdmin(message = "需要管理员权限")
    @PostMapping("/user/blacklist")
    public Result<String> setUserBlacklist(@RequestParam Long userId, 
                                           @RequestParam Boolean isBlacklist) {
        try {
            Long adminId = StpAdminUtil.getLoginIdAsLong();
            lotteryAdminService.setUserBlacklist(userId, isBlacklist, adminId);
            return Result.success(isBlacklist ? "已加入黑名单" : "已移除黑名单");
        } catch (Exception e) {
            log.error("设置用户黑名单失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 归一化所有奖品概率
     */
    @RequireAdmin(message = "需要管理员权限")
    @PostMapping("/prize/normalize")
    public Result<BigDecimal> normalizeAllProbabilities() {
        try {
            BigDecimal sum = normalizeService.normalizeAllProbabilities();
            return Result.success("归一化成功，概率总和：" + sum, sum);
        } catch (Exception e) {
            log.error("归一化失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 验证概率总和
     */
    @RequireAdmin(message = "需要管理员权限")
    @GetMapping("/prize/validate-probability")
    public Result<BigDecimal> validateProbabilitySum() {
        try {
            BigDecimal sum = normalizeService.validateProbabilitySum();
            boolean needsNorm = normalizeService.needsNormalization();
            String message = needsNorm ? "概率需要归一化，当前总和：" + sum : "概率正常，总和：" + sum;
            return Result.success(message, sum);
        } catch (Exception e) {
            log.error("验证概率失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 批量调整概率
     */
    @RequireAdmin(message = "需要管理员权限")
    @PostMapping("/prize/batch-adjust")
    public Result<String> batchAdjustProbability(@Valid @RequestBody BatchAdjustProbabilityRequest request) {
        try {
            Long adminId = StpAdminUtil.getLoginIdAsLong();
            lotteryAdminService.batchAdjustProbability(request, adminId);
            return Result.success("批量调整成功");
        } catch (Exception e) {
            log.error("批量调整概率失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 批量启用/禁用奖品
     */
    @RequireAdmin(message = "需要管理员权限")
    @PostMapping("/prize/batch-toggle")
    public Result<String> batchToggleStatus(@Valid @RequestBody BatchToggleStatusRequest request) {
        try {
            Long adminId = StpAdminUtil.getLoginIdAsLong();
            lotteryAdminService.batchToggleStatus(request, adminId);
            return Result.success(request.getIsActive() ? "批量启用成功" : "批量禁用成功");
        } catch (Exception e) {
            log.error("批量切换状态失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取风险用户列表
     */
    @RequireAdmin(message = "需要管理员权限")
    @PostMapping("/user/risk-list")
    public Result<PageResult<com.xiaou.points.domain.UserLotteryLimit>> getRiskUserList(@RequestBody RiskUserQueryRequest request) {
        try {
            PageResult<com.xiaou.points.domain.UserLotteryLimit> result = riskService.getRiskUserList(request);
            return Result.success("获取成功", result);
        } catch (Exception e) {
            log.error("获取风险用户列表失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 评估用户风险等级
     */
    @RequireAdmin(message = "需要管理员权限")
    @PostMapping("/user/evaluate-risk")
    public Result<Integer> evaluateRiskLevel(@RequestParam Long userId) {
        try {
            Integer riskLevel = riskService.evaluateRiskLevel(userId);
            return Result.success("评估成功，风险等级：" + riskLevel, riskLevel);
        } catch (Exception e) {
            log.error("评估风险等级失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 检测异常行为
     */
    @RequireAdmin(message = "需要管理员权限")
    @PostMapping("/user/detect-abnormal")
    public Result<Boolean> detectAbnormalBehavior(@RequestParam Long userId) {
        try {
            boolean hasAbnormal = riskService.detectAbnormalBehavior(userId);
            return Result.success(hasAbnormal ? "检测到异常行为" : "行为正常", hasAbnormal);
        } catch (Exception e) {
            log.error("检测异常行为失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 刷新缓存
     */
    @RequireAdmin(message = "需要管理员权限")
    @PostMapping("/cache/refresh")
    public Result<String> refreshCache() {
        try {
            cacheWarmer.refreshCache();
            return Result.success("缓存刷新成功");
        } catch (Exception e) {
            log.error("刷新缓存失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取预警信息列表
     */
    @RequireAdmin(message = "需要管理员权限")
    @GetMapping("/monitor/alerts")
    public Result<List<AlertInfo>> getAlerts() {
        try {
            List<AlertInfo> alerts = lotteryAdminService.getAlerts();
            return Result.success("获取成功", alerts);
        } catch (Exception e) {
            log.error("获取预警信息失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取综合分析数据
     */
    @RequireAdmin(message = "需要管理员权限")
    @GetMapping("/analysis/comprehensive")
    public Result<AnalysisResponse> getComprehensiveAnalysis(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            AnalysisResponse analysis = analysisService.getComprehensiveAnalysis(startDate, endDate);
            return Result.success("获取成功", analysis);
        } catch (Exception e) {
            log.error("获取分析数据失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 手动熔断
     */
    @RequireAdmin(message = "需要管理员权限")
    @PostMapping("/emergency/circuit-break")
    public Result<String> manualCircuitBreak(@RequestParam String reason) {
        try {
            emergencyService.manualCircuitBreak(reason);
            return Result.success("熔断成功");
        } catch (Exception e) {
            log.error("熔断失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 恢复服务
     */
    @RequireAdmin(message = "需要管理员权限")
    @PostMapping("/emergency/resume")
    public Result<String> resumeService() {
        try {
            emergencyService.resumeService();
            return Result.success("服务已恢复");
        } catch (Exception e) {
            log.error("恢复服务失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 启用降级模式
     */
    @RequireAdmin(message = "需要管理员权限")
    @PostMapping("/emergency/degradation/enable")
    public Result<String> enableDegradation() {
        try {
            emergencyService.enableDegradation();
            return Result.success("降级模式已启用");
        } catch (Exception e) {
            log.error("启用降级失败", e);
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 禁用降级模式
     */
    @RequireAdmin(message = "需要管理员权限")
    @PostMapping("/emergency/degradation/disable")
    public Result<String> disableDegradation() {
        try {
            emergencyService.disableDegradation();
            return Result.success("降级模式已禁用");
        } catch (Exception e) {
            log.error("禁用降级失败", e);
            return Result.error(e.getMessage());
        }
    }
}

