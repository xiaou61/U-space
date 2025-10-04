package com.xiaou.points.service.impl;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.common.utils.PageHelper;
import com.xiaou.points.domain.UserLotteryLimit;
import com.xiaou.points.dto.lottery.RiskUserQueryRequest;
import com.xiaou.points.mapper.UserLotteryLimitMapper;
import com.xiaou.points.mapper.LotteryDrawRecordMapper;
import com.xiaou.points.service.LotteryRiskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 抽奖风险管理服务实现
 * 
 * @author xiaou
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LotteryRiskServiceImpl implements LotteryRiskService {
    
    private final UserLotteryLimitMapper userLimitMapper;
    private final LotteryDrawRecordMapper drawRecordMapper;
    
    @Override
    public PageResult<UserLotteryLimit> getRiskUserList(RiskUserQueryRequest request) {
        return PageHelper.doPage(request.getPage(), request.getSize(), () -> {
            return userLimitMapper.selectRiskUsers(request);
        });
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer evaluateRiskLevel(Long userId) {
        UserLotteryLimit limit = userLimitMapper.selectByUserId(userId);
        if (limit == null) {
            return 0; // 正常
        }
        
        int riskScore = 0;
        
        // 1. 检查今日抽奖次数（超过100次 +1分）
        if (limit.getTodayDrawCount() != null && limit.getTodayDrawCount() > 100) {
            riskScore++;
        }
        
        // 2. 检查今日抽奖次数（超过200次 +2分）
        if (limit.getTodayDrawCount() != null && limit.getTodayDrawCount() > 200) {
            riskScore += 2;
        }
        
        // 3. 检查中奖率（超过90% +2分）
        if (limit.getTotalDrawCount() != null && limit.getTotalDrawCount() > 0) {
            double winRate = (double) limit.getTotalWinCount() / limit.getTotalDrawCount();
            if (winRate > 0.9) {
                riskScore += 2;
            }
        }
        
        // 4. 检查抽奖时间规律（如果最近10次抽奖间隔都很规律，+2分）
        if (hasRegularPattern(userId)) {
            riskScore += 2;
        }
        
        // 5. 检查设备切换频率（如果频繁切换设备，+1分）
        if (frequentDeviceSwitch(userId)) {
            riskScore++;
        }
        
        // 根据分数判断风险等级
        int riskLevel;
        if (riskScore == 0) {
            riskLevel = 0; // 正常
        } else if (riskScore <= 2) {
            riskLevel = 1; // 低风险
        } else if (riskScore <= 4) {
            riskLevel = 2; // 中风险
        } else {
            riskLevel = 3; // 高风险
        }
        
        // 更新风险等级
        Integer currentRiskLevel = limit.getRiskLevel();
        if (currentRiskLevel == null || currentRiskLevel != riskLevel) {
            updateUserRiskLevel(userId, riskLevel);
            log.info("用户{}风险等级更新：{} -> {}，风险分数：{}", userId, currentRiskLevel, riskLevel, riskScore);
        }
        
        return riskLevel;
    }
    
    @Override
    public boolean detectAbnormalBehavior(Long userId) {
        UserLotteryLimit limit = userLimitMapper.selectByUserId(userId);
        if (limit == null) {
            return false;
        }
        
        // 1. 检查今日抽奖次数是否异常（超过300次）
        if (limit.getTodayDrawCount() != null && limit.getTodayDrawCount() > 300) {
            log.warn("用户{}今日抽奖次数异常：{}", userId, limit.getTodayDrawCount());
            return true;
        }
        
        // 2. 检查中奖率是否异常（超过95%）
        if (limit.getTotalDrawCount() != null && limit.getTotalDrawCount() > 10) {
            double winRate = (double) limit.getTotalWinCount() / limit.getTotalDrawCount();
            if (winRate > 0.95) {
                log.warn("用户{}中奖率异常：{}%", userId, winRate * 100);
                return true;
            }
        }
        
        // 3. 检查抽奖时间间隔是否异常（机器人特征）
        if (hasRegularPattern(userId)) {
            log.warn("用户{}存在规律性抽奖行为", userId);
            return true;
        }
        
        // 4. 检查最后抽奖时间（如果在极短时间内抽奖多次）
        if (limit.getLastDrawTime() != null) {
            long seconds = ChronoUnit.SECONDS.between(limit.getLastDrawTime(), LocalDateTime.now());
            if (seconds < 1 && limit.getTodayDrawCount() > 5) {
                log.warn("用户{}抽奖频率异常", userId);
                return true;
            }
        }
        
        return false;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserRiskLevel(Long userId, Integer riskLevel) {
        UserLotteryLimit limit = userLimitMapper.selectByUserId(userId);
        if (limit != null) {
            limit.setRiskLevel(riskLevel);
            userLimitMapper.updateById(limit);
        }
    }
    
    /**
     * 检查是否有规律性抽奖模式（机器人特征）
     */
    private boolean hasRegularPattern(Long userId) {
        // 简化实现：检查最近10次抽奖记录的时间间隔
        // 实际生产环境需要更复杂的算法
        try {
            List<LocalDateTime> recentDrawTimes = drawRecordMapper.selectRecentDrawTimes(userId, 10);
            
            if (recentDrawTimes.size() < 5) {
                return false; // 样本不足
            }
            
            // 计算时间间隔的方差，如果方差很小说明很规律
            long[] intervals = new long[recentDrawTimes.size() - 1];
            for (int i = 0; i < recentDrawTimes.size() - 1; i++) {
                intervals[i] = ChronoUnit.SECONDS.between(recentDrawTimes.get(i + 1), recentDrawTimes.get(i));
            }
            
            // 计算平均值
            long sum = 0;
            for (long interval : intervals) {
                sum += interval;
            }
            double avg = (double) sum / intervals.length;
            
            // 计算方差
            double variance = 0;
            for (long interval : intervals) {
                variance += Math.pow(interval - avg, 2);
            }
            variance /= intervals.length;
            
            // 如果方差小于10（说明时间间隔非常规律），且平均间隔小于10秒，判定为机器人
            return variance < 10 && avg < 10;
            
        } catch (Exception e) {
            log.error("检查规律性抽奖模式失败", e);
            return false;
        }
    }
    
    /**
     * 检查是否频繁切换设备
     */
    private boolean frequentDeviceSwitch(Long userId) {
        try {
            // 获取最近20次抽奖的设备信息
            List<String> recentDevices = drawRecordMapper.selectRecentDevices(userId, 20);
            
            if (recentDevices.size() < 10) {
                return false; // 样本不足
            }
            
            // 计算设备种类数
            long distinctDevices = recentDevices.stream().distinct().count();
            
            // 如果最近20次抽奖使用了5种以上不同设备，判定为异常
            return distinctDevices >= 5;
            
        } catch (Exception e) {
            log.error("检查设备切换频率失败", e);
            return false;
        }
    }
}

