package com.xiaou.points.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 抽奖策略枚举
 * 
 * @author xiaou
 */
@Getter
@AllArgsConstructor
public enum LotteryStrategyEnum {
    
    ALIAS_METHOD("ALIAS_METHOD", "别名算法", "Alias Method优化算法，O(1)时间复杂度"),
    DYNAMIC_WEIGHT("DYNAMIC_WEIGHT", "动态权重", "根据用户行为动态调整权重"),
    GUARANTEE("GUARANTEE", "保底策略", "连续未中奖后必中");
    
    private final String code;
    private final String desc;
    private final String detail;
    
    /**
     * 根据code获取枚举
     */
    public static LotteryStrategyEnum getByCode(String code) {
        for (LotteryStrategyEnum strategy : values()) {
            if (strategy.getCode().equals(code)) {
                return strategy;
            }
        }
        return null;
    }
}

