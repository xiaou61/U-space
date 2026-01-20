package com.xiaou.flashcard.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 闪卡内容类型枚举
 *
 * @author xiaou
 */
@Getter
@AllArgsConstructor
public enum CardContentType {

    TEXT(1, "文本"),
    MARKDOWN(2, "Markdown"),
    CODE(3, "代码");

    private final int code;
    private final String name;

    public static CardContentType fromCode(int code) {
        for (CardContentType type : values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return TEXT;
    }
}
