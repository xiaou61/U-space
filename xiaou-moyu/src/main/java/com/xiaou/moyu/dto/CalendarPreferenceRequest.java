package com.xiaou.moyu.dto;

import lombok.Data;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;
import java.util.List;

/**
 * 日历偏好设置请求对象
 *
 * @author xiaou
 */
@Data
public class CalendarPreferenceRequest {
    
    /**
     * 事件提醒：0-关闭，1-开启
     */
    @NotNull(message = "事件提醒设置不能为空")
    @Min(value = 0, message = "事件提醒设置值无效")
    @Max(value = 1, message = "事件提醒设置值无效")
    private Integer eventReminder;
    
    /**
     * 每日内容推送：0-关闭，1-开启
     */
    @NotNull(message = "每日内容推送设置不能为空")
    @Min(value = 0, message = "每日内容推送设置值无效")
    @Max(value = 1, message = "每日内容推送设置值无效")
    private Integer dailyContentPush;
    
    /**
     * 偏好编程语言
     */
    private List<String> preferredLanguages;
    
    /**
     * 偏好内容类型：1-编程格言，2-技术小贴士，3-代码片段，4-历史上的今天
     */
    private List<Integer> preferredContentTypes;
    
    /**
     * 难度偏好：1-初级，2-中级，3-高级
     */
    @Min(value = 1, message = "难度偏好值无效")
    @Max(value = 3, message = "难度偏好值无效")
    private Integer difficultyPreference;
    
    /**
     * 通知时间
     */
    private LocalTime notificationTime;
}
