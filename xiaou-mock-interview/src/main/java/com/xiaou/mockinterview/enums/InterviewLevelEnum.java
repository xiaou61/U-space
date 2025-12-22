package com.xiaou.mockinterview.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 面试难度级别枚举
 *
 * @author xiaou
 */
@Getter
@AllArgsConstructor
public enum InterviewLevelEnum {

    JUNIOR(1, "初级", "1-3年经验"),
    MIDDLE(2, "中级", "3-5年经验"),
    SENIOR(3, "高级", "5年以上经验");

    /**
     * 级别代码
     */
    private final int code;

    /**
     * 级别名称
     */
    private final String name;

    /**
     * 级别描述
     */
    private final String description;

    /**
     * 根据代码获取枚举
     */
    public static InterviewLevelEnum getByCode(int code) {
        for (InterviewLevelEnum level : values()) {
            if (level.getCode() == code) {
                return level;
            }
        }
        return null;
    }

    /**
     * 获取显示文本
     */
    public String getDisplayText() {
        return this.name + "（" + this.description + "）";
    }
}
