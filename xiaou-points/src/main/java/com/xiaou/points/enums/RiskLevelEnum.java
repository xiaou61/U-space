package com.xiaou.points.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 风险等级枚举
 * 
 * @author xiaou
 */
@Getter
@AllArgsConstructor
public enum RiskLevelEnum {
    
    NORMAL(0, "正常", "正常抽奖行为，无异常"),
    LOW(1, "低风险", "抽奖频率略高，但行为正常"),
    MEDIUM(2, "中风险", "存在可疑行为，需要监控"),
    HIGH(3, "高风险", "明显异常行为，限制抽奖");
    
    private final int code;
    private final String desc;
    private final String detail;
    
    /**
     * 根据code获取枚举
     */
    public static RiskLevelEnum getByCode(int code) {
        for (RiskLevelEnum level : values()) {
            if (level.getCode() == code) {
                return level;
            }
        }
        return null;
    }
}

