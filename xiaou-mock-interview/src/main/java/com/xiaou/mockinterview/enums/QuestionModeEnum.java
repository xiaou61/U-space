package com.xiaou.mockinterview.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 出题模式枚举
 *
 * @author xiaou
 */
@Getter
@AllArgsConstructor
public enum QuestionModeEnum {

    LOCAL(1, "本地题库", "从题库中抽取题目"),
    AI(2, "AI出题", "由AI根据方向和难度生成题目");

    /**
     * 模式代码
     */
    private final int code;

    /**
     * 模式名称
     */
    private final String name;

    /**
     * 模式描述
     */
    private final String description;

    /**
     * 根据代码获取枚举
     */
    public static QuestionModeEnum getByCode(int code) {
        for (QuestionModeEnum mode : values()) {
            if (mode.getCode() == code) {
                return mode;
            }
        }
        return LOCAL; // 默认本地题库
    }
}
