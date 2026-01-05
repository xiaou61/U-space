package com.xiaou.team.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 打卡任务类型枚举
 * 
 * @author xiaou
 */
@Getter
@AllArgsConstructor
public enum TaskType {
    
    CODING(1, "刷题"),
    DURATION(2, "学习时长"),
    READING(3, "阅读"),
    CUSTOM(4, "自定义");
    
    private final Integer code;
    private final String name;
    
    public static TaskType getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TaskType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
