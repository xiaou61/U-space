package com.xiaou.points.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * 打卡位图工具类
 * 使用位图存储打卡记录，高效节省空间
 * 
 * @author xiaou
 */
public class CheckinBitmapUtil {
    
    /**
     * 设置某天的打卡状态
     * @param bitmap 当前位图
     * @param day 当月第几天（1-31）
     * @return 更新后的位图
     */
    public static long setBit(long bitmap, int day) {
        if (day < 1 || day > 31) {
            throw new IllegalArgumentException("day must be between 1 and 31");
        }
        return bitmap | (1L << (day - 1));
    }
    
    /**
     * 检查某天是否已打卡
     * @param bitmap 位图
     * @param day 当月第几天（1-31）
     * @return 是否已打卡
     */
    public static boolean isCheckedIn(long bitmap, int day) {
        if (day < 1 || day > 31) {
            return false;
        }
        return (bitmap & (1L << (day - 1))) != 0;
    }
    
    /**
     * 计算当月打卡天数
     * @param bitmap 位图
     * @return 打卡天数
     */
    public static int countCheckinDays(long bitmap) {
        return Long.bitCount(bitmap);
    }
    
    /**
     * 清除某天的打卡状态
     * @param bitmap 当前位图
     * @param day 当月第几天（1-31）
     * @return 更新后的位图
     */
    public static long clearBit(long bitmap, int day) {
        if (day < 1 || day > 31) {
            throw new IllegalArgumentException("day must be between 1 and 31");
        }
        return bitmap & ~(1L << (day - 1));
    }
    
    /**
     * 获取打卡的日期列表
     * @param bitmap 位图
     * @return 打卡日期列表
     */
    public static List<Integer> getCheckinDays(long bitmap) {
        List<Integer> days = new ArrayList<>();
        for (int day = 1; day <= 31; day++) {
            if (isCheckedIn(bitmap, day)) {
                days.add(day);
            }
        }
        return days;
    }
    
    /**
     * 获取位图的二进制字符串（用于调试）
     * @param bitmap 位图
     * @return 二进制字符串
     */
    public static String toBinaryString(long bitmap) {
        return String.format("%31s", Long.toBinaryString(bitmap)).replace(' ', '0');
    }
    
    /**
     * 获取当月最后打卡的日期
     * @param bitmap 位图
     * @return 最后打卡的日期，如果没有打卡返回0
     */
    public static int getLastCheckinDay(long bitmap) {
        for (int day = 31; day >= 1; day--) {
            if (isCheckedIn(bitmap, day)) {
                return day;
            }
        }
        return 0;
    }
    
    /**
     * 检查是否连续打卡到某一天
     * @param bitmap 位图
     * @param endDay 结束日期
     * @return 连续打卡的天数
     */
    public static int getContinuousDaysUntil(long bitmap, int endDay) {
        int continuousDays = 0;
        for (int day = endDay; day >= 1; day--) {
            if (isCheckedIn(bitmap, day)) {
                continuousDays++;
            } else {
                break;
            }
        }
        return continuousDays;
    }
}
