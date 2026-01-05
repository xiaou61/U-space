package com.xiaou.team.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 成员状态枚举
 * 
 * @author xiaou
 */
@Getter
@AllArgsConstructor
public enum MemberStatus {
    
    QUIT(0, "已退出"),
    NORMAL(1, "正常"),
    MUTED(2, "禁言中");
    
    private final Integer code;
    private final String name;
    
    public static MemberStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (MemberStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
