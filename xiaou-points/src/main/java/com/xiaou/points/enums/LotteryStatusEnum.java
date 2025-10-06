package com.xiaou.points.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 抽奖状态枚举
 * 
 * @author xiaou
 */
@Getter
@AllArgsConstructor
public enum LotteryStatusEnum {
    
    SUCCESS(1, "成功"),
    FAILED(2, "失败"),
    COMPENSATED(3, "已补偿");
    
    private final int code;
    private final String desc;
    
    /**
     * 根据code获取枚举
     */
    public static LotteryStatusEnum getByCode(int code) {
        for (LotteryStatusEnum status : values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        return null;
    }
}

