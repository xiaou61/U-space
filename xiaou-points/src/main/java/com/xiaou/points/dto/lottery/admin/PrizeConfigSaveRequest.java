package com.xiaou.points.dto.lottery.admin;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 奖品配置保存请求DTO
 * 
 * @author xiaou
 */
@Data
public class PrizeConfigSaveRequest {
    
    /**
     * 奖品ID（更新时必填）
     */
    private Long id;
    
    /**
     * 奖品名称
     */
    private String prizeName;
    
    /**
     * 奖品等级
     */
    private Integer prizeLevel;
    
    /**
     * 奖励积分
     */
    private Integer prizePoints;
    
    /**
     * 基础概率
     */
    private BigDecimal baseProbability;
    
    /**
     * 当前概率
     */
    private BigDecimal currentProbability;
    
    /**
     * 目标回报率
     */
    private BigDecimal targetReturnRate;
    
    /**
     * 最大回报率阈值
     */
    private BigDecimal maxReturnRate;
    
    /**
     * 最小回报率阈值
     */
    private BigDecimal minReturnRate;
    
    /**
     * 每日库存
     */
    private Integer dailyStock;
    
    /**
     * 总库存
     */
    private Integer totalStock;
    
    /**
     * 显示顺序
     */
    private Integer displayOrder;
    
    /**
     * 奖品图标
     */
    private String prizeIcon;
    
    /**
     * 奖品描述
     */
    private String prizeDesc;
    
    /**
     * 调整策略
     */
    private String adjustStrategy;
    
    /**
     * 是否启用
     */
    private Integer isActive;
}

