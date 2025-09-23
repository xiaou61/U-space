package com.xiaou.moyu.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 月份日历响应对象
 *
 * @author xiaou
 */
@Data
public class CalendarMonthDto {
    
    /**
     * 年份
     */
    private Integer year;
    
    /**
     * 月份
     */
    private Integer month;
    
    /**
     * 月份名称（如：2024年01月）
     */
    private String monthName;
    
    /**
     * 当月事件列表，按日期分组
     * Key: 日期(dd格式，如"01", "15")
     * Value: 当日事件列表
     */
    private Map<String, List<CalendarEventDto>> eventsByDate;
    
    /**
     * 重要事件标记日期列表
     */
    private List<String> majorEventDates;
    
    /**
     * 当月总事件数
     */
    private Integer totalEvents;
}
