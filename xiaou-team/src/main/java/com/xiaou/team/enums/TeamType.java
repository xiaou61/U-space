package com.xiaou.team.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 小组类型枚举
 * 
 * @author xiaou
 */
@Getter
@AllArgsConstructor
public enum TeamType {
    
    GOAL(1, "目标型", "🎯"),
    STUDY(2, "学习型", "📚"),
    CHECKIN(3, "打卡型", "🏃");
    
    private final Integer code;
    private final String name;
    private final String icon;
    
    public static TeamType getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TeamType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
