package com.xiaou.notify.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息通知类型枚举
 */
@Getter
@AllArgsConstructor
public enum NotificationTypeEnum {

    LIKE("LIKE", "点赞"),
    COMMENT("COMMENT", "评论"),
    REPLY("REPLY", "回复"),
    SYSTEM("SYSTEM", "系统通知");

    private final String code;
    private final String description;

    /**
     * 根据 code 获取枚举
     */
    public static NotificationTypeEnum fromCode(String code) {
        for (NotificationTypeEnum type : values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }
}
