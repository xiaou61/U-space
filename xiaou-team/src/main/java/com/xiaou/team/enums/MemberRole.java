package com.xiaou.team.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 成员角色枚举
 * 
 * @author xiaou
 */
@Getter
@AllArgsConstructor
public enum MemberRole {
    
    LEADER(1, "组长"),
    ADMIN(2, "管理员"),
    MEMBER(3, "成员");
    
    private final Integer code;
    private final String name;
    
    public static MemberRole getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (MemberRole role : values()) {
            if (role.getCode().equals(code)) {
                return role;
            }
        }
        return null;
    }
}
