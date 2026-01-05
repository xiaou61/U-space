package com.xiaou.team.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 小组状态枚举
 * 
 * @author xiaou
 */
@Getter
@AllArgsConstructor
public enum TeamStatus {
    
    DISSOLVED(0, "已解散"),
    NORMAL(1, "正常"),
    FULL(2, "已满员");
    
    private final Integer code;
    private final String name;
    
    public static TeamStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (TeamStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
