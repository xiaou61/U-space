package com.xiaou.hot.model.enums;

import cn.hutool.core.util.ObjectUtil;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public enum CategoryTypeEnum {
    GENERAL_DISCUSSION("综合资讯 & 讨论社区", 1),
    TECH_PROGRAMMING("技术 & 编程", 2),
    VIDEO_ENTERTAINMENT("视频 & 娱乐", 3),
    MUSIC_HOT("音乐热榜", 4),
    GOODS_SHARE("好物分享", 5),
    SPORTS("体育赛事", 6);


    private final String text;

    private final Integer value;

    CategoryTypeEnum(String text, Integer value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     */
    public static CategoryTypeEnum getEnumByValue(Integer value) {
        if (ObjectUtil.isEmpty(value)) {
            throw new RuntimeException("热榜更新间隔枚举参数为空");
        }
        for (CategoryTypeEnum anEnum : CategoryTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        throw new RuntimeException("热榜更新间隔枚举参数错误");
    }

    /**
     * 获取值列表
     */
    public static List<Integer> getValues() {
        return Arrays.stream(values()).map(item -> item.value).collect(Collectors.toList());
    }

}