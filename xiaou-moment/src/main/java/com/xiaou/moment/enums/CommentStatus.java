package com.xiaou.moment.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 评论状态枚举
 */
@Getter
@AllArgsConstructor
public enum CommentStatus {
    
    DELETED(0, "已删除"),
    NORMAL(1, "正常");
    
    private final Integer code;
    private final String desc;
} 