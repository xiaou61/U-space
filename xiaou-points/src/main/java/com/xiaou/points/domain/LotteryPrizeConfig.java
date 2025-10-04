package com.xiaou.points.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 抽奖奖品配置实体类
 * 
 * @author xiaou
 */
@Data
public class LotteryPrizeConfig {
    
    /**
     * 奖品ID
     */
    private Long id;
    
    /**
     * 奖品名称
     */
    private String prizeName;
    
    /**
     * 奖品等级：1-特等奖 2-一等奖 ... 8-未中奖
     */
    private Integer prizeLevel;
    
    /**
     * 奖励积分
     */
    private Integer prizePoints;
    
    /**
     * 基础中奖概率（0-1之间）
     */
    private BigDecimal baseProbability;
    
    /**
     * 当前动态概率（0-1之间）
     */
    private BigDecimal currentProbability;
    
    /**
     * 目标回报率（如0.0100=1%）
     */
    private BigDecimal targetReturnRate;
    
    /**
     * 最大回报率阈值（如0.0150=1.5%）
     */
    private BigDecimal maxReturnRate;
    
    /**
     * 最小回报率阈值（如0.0050=0.5%）
     */
    private BigDecimal minReturnRate;
    
    /**
     * 实际回报率
     */
    private BigDecimal actualReturnRate;
    
    /**
     * 总抽奖次数（作为分母）
     */
    private Integer totalDrawCount;
    
    /**
     * 总中奖次数
     */
    private Integer totalWinCount;
    
    /**
     * 今日抽奖次数
     */
    private Integer todayDrawCount;
    
    /**
     * 今日中奖次数
     */
    private Integer todayWinCount;
    
    /**
     * 每日库存（-1表示无限制）
     */
    private Integer dailyStock;
    
    /**
     * 总库存（-1表示无限制）
     */
    private Integer totalStock;
    
    /**
     * 当前剩余库存
     */
    private Integer currentStock;
    
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
     * 是否启用：0-禁用 1-启用
     */
    private Integer isActive;
    
    /**
     * 是否暂停：0-正常 1-暂停（回报率超标）
     */
    private Integer isSuspended;
    
    /**
     * 暂停原因
     */
    private String suspendReason;
    
    /**
     * 暂停至某时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime suspendUntil;
    
    /**
     * 调整策略：AUTO-自动 MANUAL-手动 FIXED-固定
     */
    private String adjustStrategy;
    
    /**
     * 最后调整时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastAdjustTime;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}

