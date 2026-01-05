package com.xiaou.team.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 重复类型枚举
 * 
 * @author xiaou
 */
@Getter
@AllArgsConstructor
public enum RepeatType {
    
    DAILY(1, "每日"),
    WORKDAY(2, "工作日"),
    CUSTOM(3, "自定义");
    
    private final Integer code;
    private final String name;
    
    public static RepeatType getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (RepeatType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
