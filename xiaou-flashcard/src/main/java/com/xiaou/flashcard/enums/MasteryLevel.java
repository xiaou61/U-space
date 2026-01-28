package com.xiaou.flashcard.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 闪卡掌握度等级枚举
 *
 * @author xiaou
 */
@Getter
@AllArgsConstructor
public enum MasteryLevel {

    NEW(1, "新卡", "还未学习过"),
    LEARNING(2, "学习中", "正在学习，需要复习"),
    MASTERED(3, "已掌握", "已经完全掌握");

    private final int code;
    private final String name;
    private final String desc;

    public static MasteryLevel fromCode(int code) {
        for (MasteryLevel level : values()) {
            if (level.getCode() == code) {
                return level;
            }
        }
        return NEW;
    }
}
