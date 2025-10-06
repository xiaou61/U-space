package com.xiaou.points.dto.lottery.admin;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 手动调整概率请求DTO
 * 
 * @author xiaou
 */
@Data
public class AdjustProbabilityRequest {
    
    /**
     * 奖品ID
     */
    private Long prizeId;
    
    /**
     * 新概率
     */
    private BigDecimal newProbability;
    
    /**
     * 调整原因
     */
    private String adjustReason;
    
    /**
     * 是否立即生效
     */
    private Boolean effectiveImmediately = true;
    
    /**
     * 获取调整原因（别名方法）
     */
    public String getReason() {
        return adjustReason;
    }
}

