package com.xiaou.moyu.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 程序员日历事件实体
 *
 * @author xiaou
 */
@Data
public class DeveloperCalendarEvent {
    
    /**
     * 事件ID
     */
    private Long id;
    
    /**
     * 事件名称
     */
    private String eventName;
    
    /**
     * 事件日期(MM-dd格式，用于每年循环)
     */
    private String eventDate;
    
    /**
     * 事件类型：1-程序员节日，2-技术纪念日，3-开源节日
     */
    private Integer eventType;
    
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
     * 是否重要节日：0-普通，1-重要
     */
    private Integer isMajor;
    
    /**
     * 节日祝福语
     */
    private String blessingText;
    
    /**
     * 相关链接JSON字符串（业务层转换为List）
     */
    private String relatedLinks;
    
    /**
     * 排序值
     */
    private Integer sortOrder;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
