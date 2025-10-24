package com.xiaou.codepen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 收益统计响应
 * 
 * @author xiaou
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncomeStatsResponse {
    
    /**
     * 作品总数
     */
    private Integer totalPens;
    
    /**
     * 总Fork数
     */
    private Integer totalForks;
    
    /**
     * 累计收益积分
     */
    private Integer totalIncome;
    
    /**
     * 作品收益明细列表
     */
    private List<PenIncomeDetail> penIncomes;
    
    /**
     * 作品收益明细
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PenIncomeDetail {
        /**
         * 作品ID
         */
        private Long penId;
        
        /**
         * 作品标题
         */
        private String title;
        
        /**
         * Fork次数
         */
        private Integer forkCount;
        
        /**
         * 收益积分
         */
        private Integer income;
        
        /**
         * Fork价格
         */
        private Integer forkPrice;
    }
}

