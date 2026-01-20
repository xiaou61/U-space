package com.xiaou.flashcard.algorithm;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * SM-2 间隔重复算法实现
 * 
 * SuperMemo 2 (SM-2) 是最经典的间隔重复算法，被 Anki 等软件广泛使用。
 * 
 * 核心公式：
 * - EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
 * - 其中 q 是用户评分 (0-5)，EF 是难度因子
 * 
 * 间隔计算：
 * - 第一次正确：1天
 * - 第二次正确：6天
 * - 之后：interval * EF
 *
 * @author xiaou
 */
public class SM2Algorithm {

    /**
     * 最小难度因子
     */
    private static final double MIN_EASE_FACTOR = 1.30;

    /**
     * 默认难度因子
     */
    public static final double DEFAULT_EASE_FACTOR = 2.50;

    /**
     * 最大间隔天数
     */
    private static final int MAX_INTERVAL_DAYS = 365;

    /**
     * SM-2 算法计算结果
     */
    @Data
    @AllArgsConstructor
    public static class SM2Result {
        /**
         * 新的连续正确次数
         */
        private int repetitions;

        /**
         * 新的难度因子
         */
        private double easeFactor;

        /**
         * 新的间隔天数
         */
        private int intervalDays;

        /**
         * 掌握度等级: 1-新卡 2-学习中 3-已掌握
         */
        private int masteryLevel;

        /**
         * 是否正确
         */
        private boolean correct;
    }

    /**
     * 计算SM-2算法结果
     *
     * @param repetitions 当前连续正确次数
     * @param easeFactor  当前难度因子
     * @param interval    当前间隔天数
     * @param quality     用户评分 (0-5)
     *                    0 - 完全不记得
     *                    1 - 错误，但看到答案后想起来
     *                    2 - 错误，但答案很容易记住
     *                    3 - 正确，但很费力
     *                    4 - 正确，稍有犹豫
     *                    5 - 完美，秒答
     * @return SM2结果
     */
    public static SM2Result calculate(int repetitions, double easeFactor,
                                      int interval, int quality) {
        // 边界检查
        quality = Math.max(0, Math.min(5, quality));
        if (easeFactor <= 0) {
            easeFactor = DEFAULT_EASE_FACTOR;
        }

        double newEF;
        int newInterval;
        int newRepetitions;
        boolean isCorrect = quality >= 3;

        if (isCorrect) {
            // 回答正确
            if (repetitions == 0) {
                newInterval = 1;
            } else if (repetitions == 1) {
                newInterval = 6;
            } else {
                // interval * EF
                newInterval = (int) Math.round(interval * easeFactor);
            }
            newRepetitions = repetitions + 1;
        } else {
            // 回答错误，重置
            newInterval = 1;
            newRepetitions = 0;
        }

        // 限制最大间隔
        newInterval = Math.min(newInterval, MAX_INTERVAL_DAYS);

        // 更新难度因子
        // EF' = EF + (0.1 - (5 - q) * (0.08 + (5 - q) * 0.02))
        double q = quality;
        double efDelta = 0.1 - (5 - q) * (0.08 + (5 - q) * 0.02);
        newEF = easeFactor + efDelta;

        // 难度因子最小值限制
        if (newEF < MIN_EASE_FACTOR) {
            newEF = MIN_EASE_FACTOR;
        }
        // 保留两位小数
        newEF = Math.round(newEF * 100.0) / 100.0;

        // 计算掌握度
        int masteryLevel = calculateMasteryLevel(newRepetitions);

        return new SM2Result(newRepetitions, newEF, newInterval, masteryLevel, isCorrect);
    }

    /**
     * 简化版评分 - 将4级评分转换为SM-2的0-5评分
     *
     * @param simpleQuality 简化评分 (1-4)
     *                      1 - 不会 -> SM-2: 0
     *                      2 - 模糊 -> SM-2: 2
     *                      3 - 熟悉 -> SM-2: 4
     *                      4 - 已掌握 -> SM-2: 5
     * @return SM-2 评分 (0-5)
     */
    public static int convertToSM2Quality(int simpleQuality) {
        return switch (simpleQuality) {
            case 1 -> 0;  // 不会
            case 2 -> 2;  // 模糊
            case 3 -> 4;  // 熟悉
            case 4 -> 5;  // 已掌握
            default -> 3; // 默认
        };
    }

    /**
     * 根据连续正确次数判断掌握度
     *
     * @param repetitions 连续正确次数
     * @return 掌握度等级 (1-新卡, 2-学习中, 3-已掌握)
     */
    public static int calculateMasteryLevel(int repetitions) {
        if (repetitions == 0) {
            return 1; // 新卡
        } else if (repetitions < 3) {
            return 2; // 学习中
        } else {
            return 3; // 已掌握
        }
    }
}
