package com.xiaou.moment.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 动态状态枚举
 */
@Getter
@AllArgsConstructor
public enum MomentStatus {
    
    DELETED(0, "已删除"),
    NORMAL(1, "正常"),
    REVIEWING(2, "审核中");
    
    private final Integer code;
    private final String desc;
} 