package com.xiaou.mockinterview.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 问答状态枚举
 *
 * @author xiaou
 */
@Getter
@AllArgsConstructor
public enum QAStatusEnum {

    PENDING(0, "待回答"),
    ANSWERED(1, "已回答"),
    SKIPPED(2, "已跳过");

    /**
     * 状态代码
     */
    private final int code;

    /**
     * 状态名称
     */
    private final String name;

    /**
     * 根据代码获取枚举
     */
    public static QAStatusEnum getByCode(int code) {
        for (QAStatusEnum status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return null;
    }
}
