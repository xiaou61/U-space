package com.xiaou.mockinterview.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 面试会话状态枚举
 *
 * @author xiaou
 */
@Getter
@AllArgsConstructor
public enum SessionStatusEnum {

    ONGOING(0, "进行中"),
    COMPLETED(1, "已完成"),
    INTERRUPTED(2, "已中断");

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
    public static SessionStatusEnum getByCode(int code) {
        for (SessionStatusEnum status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return null;
    }

    /**
     * 判断是否为结束状态
     */
    public boolean isFinished() {
        return this == COMPLETED || this == INTERRUPTED;
    }
}
