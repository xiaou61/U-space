package com.xiaou.points.service;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.points.domain.UserLotteryLimit;
import com.xiaou.points.dto.lottery.RiskUserQueryRequest;

/**
 * 抽奖风险管理服务接口
 * 
 * @author xiaou
 */
public interface LotteryRiskService {
    
    /**
     * 获取风险用户列表
     */
    PageResult<UserLotteryLimit> getRiskUserList(RiskUserQueryRequest request);
    
    /**
     * 评估用户风险等级
     * 
     * @param userId 用户ID
     * @return 风险等级：0-正常 1-低风险 2-中风险 3-高风险
     */
    Integer evaluateRiskLevel(Long userId);
    
    /**
     * 检测异常行为
     * 
     * @param userId 用户ID
     * @return true-存在异常行为，false-正常
     */
    boolean detectAbnormalBehavior(Long userId);
    
    /**
     * 更新用户风险等级
     */
    void updateUserRiskLevel(Long userId, Integer riskLevel);
}

