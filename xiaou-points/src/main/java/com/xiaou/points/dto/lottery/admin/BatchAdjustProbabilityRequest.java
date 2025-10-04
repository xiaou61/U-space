package com.xiaou.points.dto.lottery.admin;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 批量调整概率请求
 * 
 * @author xiaou
 */
@Data
public class BatchAdjustProbabilityRequest {
    
    /**
     * 奖品配置列表
     */
    @NotEmpty(message = "奖品列表不能为空")
    private List<PrizeAdjustItem> prizes;
    
    /**
     * 调整原因
     */
    private String reason;
    
    /**
     * 是否自动归一化
     */
    private Boolean autoNormalize = true;
    
    /**
     * 奖品调整项
     */
    @Data
    public static class PrizeAdjustItem {
        /**
         * 奖品ID
         */
        private Long prizeId;
        
        /**
         * 新概率
         */
        private BigDecimal newProbability;
    }
}

