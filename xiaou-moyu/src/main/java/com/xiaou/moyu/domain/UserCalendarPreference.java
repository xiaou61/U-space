package com.xiaou.moyu.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * 用户日历偏好设置实体
 *
 * @author xiaou
 */
@Data
public class UserCalendarPreference {
    
    /**
     * 偏好ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 事件提醒：0-关闭，1-开启
     */
    private Integer eventReminder;
    
    /**
     * 每日内容推送：0-关闭，1-开启
     */
    private Integer dailyContentPush;
    
    /**
     * 偏好编程语言JSON字符串（业务层转换为List）
     */
    private String preferredLanguages;
    
    /**
     * 偏好内容类型JSON字符串（业务层转换为List）
     */
    private String preferredContentTypes;
    
    /**
     * 难度偏好：1-初级，2-中级，3-高级
     */
    private Integer difficultyPreference;
    
    /**
     * 通知时间
     */
    @JsonFormat(pattern = "HH:mm:ss", timezone = "GMT+8")
    private LocalTime notificationTime;
    
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
