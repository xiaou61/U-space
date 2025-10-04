package com.xiaou.points.dto.lottery;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 奖品信息响应DTO
 * 
 * @author xiaou
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LotteryPrizeResponse {
    
    /**
     * 奖品ID
     */
    private Long prizeId;
    
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
     * 中奖概率
     */
    private BigDecimal probability;
    
    /**
     * 概率显示（如"0.1%"）
     */
    private String probabilityDisplay;
    
    /**
     * 奖品图标
     */
    private String prizeIcon;
    
    /**
     * 奖品描述
     */
    private String prizeDesc;
    
    /**
     * 显示顺序
     */
    private Integer displayOrder;
}

