package com.xiaou.moyu.dto;

import lombok.Data;

import java.util.List;

/**
 * 日历事件响应对象
 *
 * @author xiaou
 */
@Data
public class CalendarEventDto {
    
    /**
     * 事件ID
     */
    private Long id;
    
    /**
     * 事件名称
     */
    private String eventName;
    
    /**
     * 事件日期(MM-dd格式)
     */
    private String eventDate;
    
    /**
     * 事件类型：1-程序员节日，2-技术纪念日，3-开源节日
     */
    private Integer eventType;
    
    /**
     * 事件类型名称
     */
    private String eventTypeName;
    
    /**
     * 事件描述
     */
    private String description;
    
    /**
     * 图标标识
     */
    private String icon;
    
    /**
     * 标记颜色
     */
    private String color;
    
    /**
     * 是否重要节日
     */
    private Boolean isMajor;
    
    /**
     * 节日祝福语
     */
    private String blessingText;
    
    /**
     * 相关链接
     */
    private List<String> relatedLinks;
    
    /**
     * 是否已收藏
     */
    private Boolean isCollected;
}
