package com.xiaou.team.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 讨论分类枚举
 * 
 * @author xiaou
 */
@Getter
@AllArgsConstructor
public enum DiscussionCategory {
    
    ANNOUNCEMENT(1, "公告", "📢"),
    QUESTION(2, "问题求助", "❓"),
    NOTE(3, "学习笔记", "📝"),
    EXPERIENCE(4, "经验分享", "💡"),
    CHAT(5, "闲聊灌水", "💬");
    
    private final Integer code;
    private final String name;
    private final String icon;
    
    public static DiscussionCategory getByCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (DiscussionCategory category : values()) {
            if (category.getCode().equals(code)) {
                return category;
            }
        }
        return null;
    }
}
