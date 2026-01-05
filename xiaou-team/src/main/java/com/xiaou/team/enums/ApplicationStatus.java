package com.xiaou.team.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 申请状态枚举
 * 
 * @author xiaou
 */
@Getter
@AllArgsConstructor
public enum ApplicationStatus {
    
    PENDING(0, "待审核"),
    APPROVED(1, "已通过"),
    REJECTED(2, "已拒绝"),
    CANCELLED(3, "已取消");
    
    private final Integer code;
    private final String name;
    
    public static ApplicationStatus getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (ApplicationStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
