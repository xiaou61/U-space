package com.xiaou.points.service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 概率归一化服务接口
 * 
 * @author xiaou
 */
public interface LotteryNormalizeService {
    
    /**
     * 归一化所有奖品概率
     * 确保所有奖品概率之和等于1.0
     * 
     * @return 归一化后的总概率
     */
    BigDecimal normalizeAllProbabilities();
    
    /**
     * 归一化指定奖品列表的概率
     * 
     * @param prizeIds 奖品ID列表
     * @return 归一化后的总概率
     */
    BigDecimal normalizeProbabilities(List<Long> prizeIds);
    
    /**
     * 检查概率是否需要归一化
     * 
     * @return true-需要归一化，false-不需要
     */
    boolean needsNormalization();
    
    /**
     * 验证概率总和
     * 
     * @return 当前概率总和
     */
    BigDecimal validateProbabilitySum();
}

