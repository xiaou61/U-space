package com.xiaou.points.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 概率调整策略枚举
 * 
 * @author xiaou
 */
@Getter
@AllArgsConstructor
public enum AdjustStrategyEnum {
    
    AUTO("AUTO", "自动调整", "系统自动根据回报率调整概率"),
    MANUAL("MANUAL", "手动调整", "仅支持手动修改，不自动调整"),
    FIXED("FIXED", "固定模式", "概率固定，不做任何调整");
    
    private final String code;
    private final String desc;
    private final String detail;
    
    /**
     * 根据code获取枚举
     */
    public static AdjustStrategyEnum getByCode(String code) {
        for (AdjustStrategyEnum strategy : values()) {
            if (strategy.getCode().equals(code)) {
                return strategy;
            }
        }
        return null;
    }
}

