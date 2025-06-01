package com.xiaou.hot.model.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 热榜更新间隔枚举
 */
@Getter
public enum UpdateIntervalEnum {
    HALF_HOUR("半小时", BigDecimal.valueOf(0.5)),
    ONE_HOUR("1小时", BigDecimal.valueOf(1)),
    TWO_HOUR("3小时", BigDecimal.valueOf(3)),
    TWELVE_HOUR("12 小时", BigDecimal.valueOf(12)),
    ONE_DAY("24 小时", BigDecimal.valueOf(24));


    private final String text;

    private final BigDecimal value;

    UpdateIntervalEnum(String text, BigDecimal value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     */
    public static UpdateIntervalEnum getEnumByValue(BigDecimal value) {
        if (ObjectUtil.isEmpty(value)) {
            throw new RuntimeException("热榜更新间隔枚举不能为空");
        }
        for (UpdateIntervalEnum anEnum : UpdateIntervalEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        throw new RuntimeException("热榜更新间隔枚举参数不存在，请在：[" + Arrays.stream(values()).map(item -> item.value + ":" + item.text).collect(Collectors.joining(",")) + "]中选择");
    }

    /**
     * 获取值列表
     */
    public static List<BigDecimal> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

}