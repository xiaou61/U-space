package com.xiaou.points.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 奖品等级枚举
 * 
 * @author xiaou
 */
@Getter
@AllArgsConstructor
public enum PrizeLevelEnum {
    
    SPECIAL(1, "特等奖", "超级大奖"),
    FIRST(2, "一等奖", "幸运大奖"),
    SECOND(3, "二等奖", "恭喜中奖"),
    THIRD(4, "三等奖", "不错的奖品"),
    FOURTH(5, "四等奖", "保本奖"),
    FIFTH(6, "五等奖", "小奖励"),
    SIXTH(7, "六等奖", "安慰奖"),
    NO_PRIZE(8, "未中奖", "很遗憾");
    
    private final int code;
    private final String desc;
    private final String detail;
    
    /**
     * 根据code获取枚举
     */
    public static PrizeLevelEnum getByCode(int code) {
        for (PrizeLevelEnum level : values()) {
            if (level.getCode() == code) {
                return level;
            }
        }
        return null;
    }
    
    /**
     * 根据code获取描述
     */
    public static String getDescByCode(int code) {
        PrizeLevelEnum level = getByCode(code);
        return level != null ? level.getDesc() : "未知等级";
    }
    
    /**
     * 是否中奖
     */
    public boolean isWin() {
        return this != NO_PRIZE;
    }
}

