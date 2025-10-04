package com.xiaou.points.dto.lottery;

import lombok.Data;

/**
 * 抽奖请求DTO
 * 
 * @author xiaou
 */
@Data
public class LotteryDrawRequest {
    
    /**
     * 抽奖策略类型（可选）
     */
    private String strategyType;
}

