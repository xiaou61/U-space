package com.xiaou.flashcard.algorithm;

import com.xiaou.flashcard.domain.FlashcardStudyRecord;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * SM-2间隔重复算法实现
 * 
 * 基于SuperMemo SM-2算法，根据用户反馈动态调整复习间隔
 * 
 * 用户反馈等级:
 * 0 - 忘记: 完全不记得，重置间隔
 * 1 - 模糊: 记得一点，间隔缩短
 * 2 - 记得: 稍加思考后想起，正常间隔
 * 3 - 简单: 轻松记得，间隔延长
 *
 * @author xiaou
 */
public class SM2Algorithm {

    /**
     * 默认难度因子
     */
    private static final BigDecimal DEFAULT_EF = new BigDecimal("2.50");

    /**
     * 最小难度因子
     */
    private static final BigDecimal MIN_EF = new BigDecimal("1.30");

    /**
     * 最大难度因子
     */
    private static final BigDecimal MAX_EF = new BigDecimal("2.50");

    /**
     * 掌握阈值：连续正确次数达到此值认为已掌握
     */
    private static final int MASTERED_THRESHOLD = 5;

    /**
     * 计算下次复习间隔
     *
     * @param quality     用户评分 0-3
     * @param repetitions 连续正确次数
     * @param easeFactor  当前难度因子
     * @param lastInterval 上次间隔天数
     * @return 计算结果
     */
    public static SM2Result calculate(int quality, int repetitions, 
                                       BigDecimal easeFactor, int lastInterval) {
        SM2Result result = new SM2Result();
        
        // 确保难度因子有效
        if (easeFactor == null) {
            easeFactor = DEFAULT_EF;
        }

        // 计算新的难度因子
        // EF' = EF + (0.1 - (3-q) × (0.08 + (3-q) × 0.02))
        BigDecimal newEF = calculateNewEaseFactor(quality, easeFactor);
        result.setEaseFactor(newEF);

        // 根据评分计算间隔
        if (quality < 1) {
            // 忘记：重置
            result.setRepetitions(0);
            result.setIntervalDays(0);
            result.setNextReviewMinutes(10); // 10分钟后复习
            result.setNeedImmediateReview(true);
            result.setNewStatus(FlashcardStudyRecord.STATUS_LEARNING);
        } else {
            // 记住：计算新间隔
            int newRepetitions = repetitions + 1;
            result.setRepetitions(newRepetitions);
            result.setNeedImmediateReview(false);

            int interval = calculateInterval(newRepetitions, lastInterval, newEF, quality);
            result.setIntervalDays(interval);

            // 判断是否已掌握
            if (newRepetitions >= MASTERED_THRESHOLD && quality >= 2) {
                result.setNewStatus(FlashcardStudyRecord.STATUS_MASTERED);
            } else {
                result.setNewStatus(FlashcardStudyRecord.STATUS_LEARNING);
            }
        }

        return result;
    }

    /**
     * 计算新的难度因子
     */
    private static BigDecimal calculateNewEaseFactor(int quality, BigDecimal currentEF) {
        // EF' = EF + (0.1 - (3-q) × (0.08 + (3-q) × 0.02))
        int diff = 3 - quality;
        double adjustment = 0.1 - diff * (0.08 + diff * 0.02);
        
        BigDecimal newEF = currentEF.add(new BigDecimal(adjustment));
        
        // 限制在有效范围内
        if (newEF.compareTo(MIN_EF) < 0) {
            newEF = MIN_EF;
        }
        if (newEF.compareTo(MAX_EF) > 0) {
            newEF = MAX_EF;
        }
        
        return newEF.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算复习间隔天数
     */
    private static int calculateInterval(int repetitions, int lastInterval, 
                                          BigDecimal easeFactor, int quality) {
        int interval;
        
        if (repetitions == 1) {
            interval = 1;
        } else if (repetitions == 2) {
            interval = 3;
        } else {
            // interval = lastInterval × EF
            interval = new BigDecimal(lastInterval)
                    .multiply(easeFactor)
                    .setScale(0, RoundingMode.HALF_UP)
                    .intValue();
        }

        // 根据评分微调
        if (quality == 1) {
            // 模糊：间隔缩短50%
            interval = Math.max(1, interval / 2);
        } else if (quality == 3) {
            // 简单：间隔延长50%
            interval = (int) (interval * 1.5);
        }

        // 最大间隔限制为365天
        return Math.min(interval, 365);
    }

    /**
     * 获取默认难度因子
     */
    public static BigDecimal getDefaultEaseFactor() {
        return DEFAULT_EF;
    }

    /**
     * 根据评分获取描述
     */
    public static String getQualityDescription(int quality) {
        return switch (quality) {
            case 0 -> "忘记";
            case 1 -> "模糊";
            case 2 -> "记得";
            case 3 -> "简单";
            default -> "未知";
        };
    }

    /**
     * 根据评分获取预计下次复习间隔描述
     */
    public static String getIntervalDescription(int intervalDays) {
        if (intervalDays == 0) {
            return "10分钟";
        } else if (intervalDays == 1) {
            return "1天";
        } else if (intervalDays < 7) {
            return intervalDays + "天";
        } else if (intervalDays < 30) {
            return (intervalDays / 7) + "周";
        } else if (intervalDays < 365) {
            return (intervalDays / 30) + "个月";
        } else {
            return (intervalDays / 365) + "年";
        }
    }
}
