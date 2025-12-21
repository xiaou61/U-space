package com.xiaou.mockinterview.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 问题类型枚举
 *
 * @author xiaou
 */
@Getter
@AllArgsConstructor
public enum QuestionTypeEnum {

    MAIN(1, "主问题"),
    FOLLOW_UP(2, "追问");

    /**
     * 类型代码
     */
    private final int code;

    /**
     * 类型名称
     */
    private final String name;

    /**
     * 根据代码获取枚举
     */
    public static QuestionTypeEnum getByCode(int code) {
        for (QuestionTypeEnum type : values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return MAIN;
    }
}
