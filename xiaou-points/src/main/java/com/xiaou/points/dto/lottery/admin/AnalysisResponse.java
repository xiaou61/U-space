package com.xiaou.points.dto.lottery.admin;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 数据分析响应
 * 
 * @author xiaou
 */
@Data
@Builder
public class AnalysisResponse {
    
    /**
     * ROI分析
     */
    private RoiAnalysis roiAnalysis;
    
    /**
     * 用户行为分析
     */
    private UserBehaviorAnalysis userBehaviorAnalysis;
    
    /**
     * 成本效益分析
     */
    private CostBenefitAnalysis costBenefitAnalysis;
    
    @Data
    @Builder
    public static class RoiAnalysis {
        /**
         * 总投入（积分消耗）
         */
        private Long totalInput;
        
        /**
         * 总产出（奖品价值）
         */
        private Long totalOutput;
        
        /**
         * ROI
         */
        private BigDecimal roi;
        
        /**
         * 单个奖品ROI
         */
        private Map<String, BigDecimal> prizeRoi;
    }
    
    @Data
    @Builder
    public static class UserBehaviorAnalysis {
        /**
         * 活跃用户数
         */
        private Integer activeUsers;
        
        /**
         * 平均抽奖次数
         */
        private BigDecimal avgDrawCount;
        
        /**
         * 用户留存率
         */
        private BigDecimal retentionRate;
        
        /**
         * 高频用户占比
         */
        private BigDecimal highFrequencyUserRate;
    }
    
    @Data
    @Builder
    public static class CostBenefitAnalysis {
        /**
         * 平台收益
         */
        private Long platformProfit;
        
        /**
         * 成本
         */
        private Long cost;
        
        /**
         * 效益比
         */
        private BigDecimal benefitRatio;
    }
}

