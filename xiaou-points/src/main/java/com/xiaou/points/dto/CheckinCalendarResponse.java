package com.xiaou.points.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 打卡日历响应DTO
 * 
 * @author xiaou
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckinCalendarResponse {
    
    /**
     * 年月
     */
    private String yearMonth;
    
    /**
     * 打卡位图
     */
    private Long checkinBitmap;
    
    /**
     * 打卡日期列表
     */
    private List<Integer> checkinDays;
    
    /**
     * 当月总打卡天数
     */
    private Integer totalCheckinDays;
    
    /**
     * 连续打卡天数
     */
    private Integer continuousDays;
    
    /**
     * 最后打卡日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDate lastCheckinDate;
    
    /**
     * 当月统计
     */
    private MonthlyStats monthlyStats;
    
    /**
     * 日历网格数据（天数 -> 打卡信息）
     */
    private Map<String, DayInfo> calendarGrid;
    
    /**
     * 当月统计信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyStats {
        private Integer totalDays;
        private String checkinRate;
        private Integer pointsEarned;
    }
    
    /**
     * 单日信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayInfo {
        private Boolean checked;
        private Integer points;
    }
}
