package com.xiaou.points.utils;

/**
 * 打卡积分计算器
 * 实现连续打卡递增奖励机制
 * 
 * @author xiaou
 */
public class CheckinPointsCalculator {
    
    /**
     * 基础积分
     */
    private static final int BASE_POINTS = 50;
    
    /**
     * 递增积分
     */
    private static final int INCREMENT_POINTS = 10;
    
    /**
     * 周奖励积分
     */
    private static final int WEEK_BONUS = 50;
    
    /**
     * 计算连续打卡应得积分
     * 奖励规则：
     * - 连续1天：50积分（基础奖励）
     * - 连续2天：60积分（+10积分递增）
     * - 连续3天：70积分（+10积分递增）
     * - ...
     * - 连续7天：150积分（基础+递增+周奖励）
     * - 连续8天：60积分（重新开始递增）
     * 
     * @param continuousDays 连续打卡天数
     * @return 应得积分
     */
    public static int calculatePoints(int continuousDays) {
        if (continuousDays <= 0) {
            return 0;
        }
        
        // 计算在7天周期中的位置（1-7）
        int cycleDay = (continuousDays - 1) % 7 + 1;
        
        if (cycleDay == 7) {
            // 第7天：基础积分 + 递增积分 + 周奖励
            return BASE_POINTS + (cycleDay - 1) * INCREMENT_POINTS + WEEK_BONUS;
        } else {
            // 非第7天：基础积分 + 递增积分
            return BASE_POINTS + (cycleDay - 1) * INCREMENT_POINTS;
        }
    }
    
    /**
     * 计算下一天打卡可得积分
     * 
     * @param continuousDays 当前连续打卡天数
     * @return 下一天可得积分
     */
    public static int calculateNextDayPoints(int continuousDays) {
        return calculatePoints(continuousDays + 1);
    }
    
    /**
     * 获取当前周期的描述
     * 
     * @param continuousDays 连续打卡天数
     * @return 周期描述
     */
    public static String getCycleDescription(int continuousDays) {
        if (continuousDays <= 0) {
            return "尚未开始打卡";
        }
        
        int cycleDay = (continuousDays - 1) % 7 + 1;
        int week = (continuousDays - 1) / 7 + 1;
        
        if (cycleDay == 7) {
            return String.format("第%d周期的第%d天（周奖励日）", week, cycleDay);
        } else {
            return String.format("第%d周期的第%d天", week, cycleDay);
        }
    }
    
    /**
     * 判断是否为周奖励日（第7天）
     * 
     * @param continuousDays 连续打卡天数
     * @return 是否为周奖励日
     */
    public static boolean isWeekBonusDay(int continuousDays) {
        if (continuousDays <= 0) {
            return false;
        }
        return continuousDays % 7 == 0;
    }
    
    /**
     * 计算距离下个周奖励还需要多少天
     * 
     * @param continuousDays 连续打卡天数
     * @return 距离下个周奖励的天数
     */
    public static int getDaysToNextWeekBonus(int continuousDays) {
        if (continuousDays <= 0) {
            return 7;
        }
        
        int cycleDay = (continuousDays - 1) % 7 + 1;
        if (cycleDay == 7) {
            return 7; // 今天就是周奖励日，下次是7天后
        } else {
            return 7 - cycleDay;
        }
    }
}
