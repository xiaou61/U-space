package com.xiaou.moyu.dto;

import lombok.Data;

import java.util.List;

/**
 * 今日推荐响应对象
 *
 * @author xiaou
 */
@Data
public class TodayRecommendDto {
    
    /**
     * 日期（yyyy-MM-dd格式）
     */
    private String date;
    
    /**
     * 今日事件列表
     */
    private List<CalendarEventDto> todayEvents;
    
    /**
     * 今日内容推荐
     */
    private List<DailyContentDto> todayContent;
    
    /**
     * 编程格言
     */
    private DailyContentDto quote;
    
    /**
     * 技术小贴士
     */
    private DailyContentDto tip;
    
    /**
     * 代码片段
     */
    private DailyContentDto codeSnippet;
    
    /**
     * 历史上的今天
     */
    private DailyContentDto history;
    
    /**
     * 是否有重要事件
     */
    private Boolean hasMajorEvents;
    
    /**
     * 特殊问候语
     */
    private String specialGreeting;
}
