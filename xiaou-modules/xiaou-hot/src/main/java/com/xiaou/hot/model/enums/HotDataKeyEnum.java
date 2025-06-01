package com.xiaou.hot.model.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 热榜类型枚举
 *
 * @author cong
 */
@Getter
public enum HotDataKeyEnum {
    ZHI_HU("知乎", "zhiHu"),
    WEI_BO("微博", "WeiBo"),
    CODE_FATHER("编程导航", "CodeFather"),
    BILI_BILI("哔哩哔哩", "BiliBili"),
    WY_CLOUD_MUSIC("网易云音乐", "WyCloudMusic"),
    DOU_YIN("抖音", "DouYin"),
    CS_DN("csdn", "CSDN"),
    JUE_JIN("掘金", "JueJin");


    private final String text;

    private final String value;

    HotDataKeyEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     */
    public static HotDataKeyEnum getEnumByValue(String value) {
        if (ObjectUtil.isEmpty(value)) {
            throw new RuntimeException("热榜类型枚举不能为空");
        }
        for (HotDataKeyEnum anEnum : HotDataKeyEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        throw new RuntimeException( "热榜类型枚举参数不存在，请在：[" + Arrays.stream(values()).map(item -> item.value + ":" + item.text).collect(Collectors.joining(",")) + "]中选择");
    }

    /**
     * 获取值列表
     */
    public static List<String> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

}