package com.xiaou.points.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 概率调整历史实体类
 * 
 * @author xiaou
 */
@Data
public class LotteryAdjustHistory {
    
    /**
     * 记录ID
     */
    private Long id;
    
    /**
     * 奖品ID
     */
    private Long prizeId;
    
    /**
     * 调整类型：AUTO-自动 MANUAL-手动
     */
    private String adjustType;
    
    /**
     * 调整前概率
     */
    private BigDecimal oldProbability;
    
    /**
     * 调整后概率
     */
    private BigDecimal newProbability;
    
    /**
     * 调整前回报率
     */
    private BigDecimal oldReturnRate;
    
    /**
     * 调整后回报率
     */
    private BigDecimal newReturnRate;
    
    /**
     * 调整原因
     */
    private String adjustReason;
    
    /**
     * 操作人（SYSTEM或管理员名称）
     */
    private String operator;
    
    /**
     * 操作人ID（管理员ID）
     */
    private Long operatorId;
    
    /**
     * 调整时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}

