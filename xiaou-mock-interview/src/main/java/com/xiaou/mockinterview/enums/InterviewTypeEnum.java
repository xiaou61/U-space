package com.xiaou.mockinterview.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 面试类型枚举
 *
 * @author xiaou
 */
@Getter
@AllArgsConstructor
public enum InterviewTypeEnum {

    TECHNICAL(1, "技术面试", "纯技术问题"),
    COMPREHENSIVE(2, "综合面试", "技术 + 项目经验 + 软技能"),
    SPECIALIZED(3, "专项突破", "针对特定知识点");

    /**
     * 类型代码
     */
    private final int code;

    /**
     * 类型名称
     */
    private final String name;

    /**
     * 类型描述
     */
    private final String description;

    /**
     * 根据代码获取枚举
     */
    public static InterviewTypeEnum getByCode(int code) {
        for (InterviewTypeEnum type : values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        return TECHNICAL; // 默认返回技术面试
    }
}
