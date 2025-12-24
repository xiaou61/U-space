package com.xiaou.interview.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 学习热力图响应
 *
 * @author xiaou
 */
@Data
@Accessors(chain = true)
public class HeatmapResponse {

    /**
     * 年份
     */
    private Integer year;

    /**
     * 总学习天数
     */
    private Integer totalDays;

    /**
     * 当前连续学习天数
     */
    private Integer currentStreak;

    /**
     * 最长连续学习天数
     */
    private Integer longestStreak;

    /**
     * 各月学习天数统计
     */
    private Map<String, Integer> monthStats;

    /**
     * 每日学习数据
     */
    private List<DailyData> dailyData;

    /**
     * 每日学习数据
     */
    @Data
    @Accessors(chain = true)
    public static class DailyData {
        /**
         * 日期
         */
        @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
        private LocalDate date;

        /**
         * 学习题目数
         */
        private Integer count;

        /**
         * 热度等级 0-4
         */
        private Integer level;

        /**
         * 新学习数
         */
        private Integer learnCount;

        /**
         * 复习数
         */
        private Integer reviewCount;
    }

    /**
     * 根据数量计算热度等级
     */
    public static int calculateLevel(int count) {
        if (count == 0) return 0;
        if (count <= 5) return 1;
        if (count <= 15) return 2;
        if (count <= 30) return 3;
        return 4;
    }
}
