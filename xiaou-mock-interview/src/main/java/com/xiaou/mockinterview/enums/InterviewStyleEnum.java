package com.xiaou.mockinterview.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AI面试官风格枚举
 *
 * @author xiaou
 */
@Getter
@AllArgsConstructor
public enum InterviewStyleEnum {

    GENTLE(1, "温和型", "鼓励为主，适合建立信心", 1, 0.3),
    STANDARD(2, "标准型", "客观公正，模拟常规面试", 0, 0.5),
    PRESSURE(3, "压力型", "高标准严要求，模拟大厂面试", -1, 0.7);

    /**
     * 风格代码
     */
    private final int code;

    /**
     * 风格名称
     */
    private final String name;

    /**
     * 风格描述
     */
    private final String description;

    /**
     * 评分调整（正数表示宽松，负数表示严格）
     */
    private final int scoreAdjustment;

    /**
     * 追问频率（0-1之间）
     */
    private final double followUpRate;

    /**
     * 根据代码获取枚举
     */
    public static InterviewStyleEnum getByCode(int code) {
        for (InterviewStyleEnum style : values()) {
            if (style.getCode() == code) {
                return style;
            }
        }
        return STANDARD; // 默认返回标准型
    }
}
