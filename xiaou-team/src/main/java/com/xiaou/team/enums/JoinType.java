package com.xiaou.team.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 加入方式枚举
 * 
 * @author xiaou
 */
@Getter
@AllArgsConstructor
public enum JoinType {
    
    PUBLIC(1, "公开加入"),
    APPLY(2, "申请加入"),
    INVITE(3, "邀请加入");
    
    private final Integer code;
    private final String name;
    
    public static JoinType getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (JoinType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }
}
