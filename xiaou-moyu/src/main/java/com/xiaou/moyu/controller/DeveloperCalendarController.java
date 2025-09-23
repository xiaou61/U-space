package com.xiaou.moyu.controller;

import com.xiaou.common.annotation.Log;
import com.xiaou.common.core.domain.Result;
import com.xiaou.common.exception.BusinessException;
import com.xiaou.common.utils.JsonUtils;
import com.xiaou.common.utils.UserContextUtil;
import com.xiaou.moyu.domain.DeveloperCalendarEvent;
import com.xiaou.moyu.domain.UserCalendarPreference;
import com.xiaou.moyu.dto.CalendarEventDto;
import com.xiaou.moyu.dto.CalendarMonthDto;
import com.xiaou.moyu.dto.CalendarPreferenceRequest;
import com.xiaou.moyu.dto.TodayRecommendDto;
import com.xiaou.moyu.service.DeveloperCalendarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 程序员日历控制器
 *
 * @author xiaou
 */
@Slf4j
@RestController
@RequestMapping("/moyu/developer-calendar")
@RequiredArgsConstructor
public class DeveloperCalendarController {
    
    private final DeveloperCalendarService developerCalendarService;
    private final UserContextUtil userContextUtil;
    
    /**
     * 获取今日推荐
     */
    @Log(module = "摸鱼工具", type = Log.OperationType.SELECT, description = "获取今日推荐")
    @GetMapping("/today")
    public Result<TodayRecommendDto> getTodayRecommend() {
        Long userId = getCurrentUserId();
        
        TodayRecommendDto todayRecommend = new TodayRecommendDto();
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        todayRecommend.setDate(today);
        
        // 获取今日事件
        List<DeveloperCalendarEvent> todayEvents = developerCalendarService.getTodayEvents();
        List<CalendarEventDto> eventDtos = convertToEventDtos(todayEvents, userId);
        todayRecommend.setTodayEvents(eventDtos);
        
        // 检查是否有重要事件
        boolean hasMajorEvents = todayEvents.stream().anyMatch(event -> event.getIsMajor() == 1);
        todayRecommend.setHasMajorEvents(hasMajorEvents);
        
        // 设置特殊问候语
        if (hasMajorEvents) {
            DeveloperCalendarEvent majorEvent = todayEvents.stream()
                    .filter(event -> event.getIsMajor() == 1)
                    .findFirst().orElse(null);
            if (majorEvent != null && majorEvent.getBlessingText() != null) {
                todayRecommend.setSpecialGreeting(majorEvent.getBlessingText());
            }
        }
        
        return Result.success(todayRecommend);
    }
    
    /**
     * 获取指定月份的日历数据
     */
    @Log(module = "摸鱼工具", type = Log.OperationType.SELECT, description = "获取月份日历数据")
    @GetMapping("/month/{year}/{month}")
    public Result<CalendarMonthDto> getMonthCalendar(@PathVariable Integer year, 
                                                     @PathVariable Integer month) {
        Long userId = getCurrentUserId();
        
        // 格式化月份为MM格式
        String monthStr = String.format("%02d", month);
        List<DeveloperCalendarEvent> events = developerCalendarService.getEventsByMonth(monthStr);
        
        CalendarMonthDto monthDto = new CalendarMonthDto();
        monthDto.setYear(year);
        monthDto.setMonth(month);
        monthDto.setMonthName(String.format("%d年%02d月", year, month));
        monthDto.setTotalEvents(events.size());
        
        // 按日期分组事件
        Map<String, List<CalendarEventDto>> eventsByDate = new HashMap<>();
        List<String> majorEventDates = new ArrayList<>();
        
        for (DeveloperCalendarEvent event : events) {
            String day = event.getEventDate().substring(3); // 提取日期部分
            
            CalendarEventDto eventDto = convertToEventDto(event, userId);
            
            eventsByDate.computeIfAbsent(day, k -> new ArrayList<>()).add(eventDto);
            
            if (event.getIsMajor() == 1 && !majorEventDates.contains(day)) {
                majorEventDates.add(day);
            }
        }
        
        monthDto.setEventsByDate(eventsByDate);
        monthDto.setMajorEventDates(majorEventDates);
        
        return Result.success(monthDto);
    }
    
    /**
     * 获取指定日期的事件列表
     */
    @Log(module = "摸鱼工具", type = Log.OperationType.SELECT, description = "获取指定日期事件")
    @GetMapping("/events/{date}")
    public Result<List<CalendarEventDto>> getEventsByDate(@PathVariable String date) {
        Long userId = getCurrentUserId();
        
        // 转换日期格式为MM-dd
        String eventDate;
        try {
            LocalDate localDate = LocalDate.parse(date);
            eventDate = localDate.format(DateTimeFormatter.ofPattern("MM-dd"));
        } catch (Exception e) {
            throw new BusinessException("日期格式不正确，请使用yyyy-MM-dd格式");
        }
        
        List<DeveloperCalendarEvent> events = developerCalendarService.getEventsByDate(eventDate);
        List<CalendarEventDto> eventDtos = convertToEventDtos(events, userId);
        
        return Result.success(eventDtos);
    }
    
    /**
     * 根据事件类型获取事件列表
     */
    @Log(module = "摸鱼工具", type = Log.OperationType.SELECT, description = "根据类型获取事件")
    @GetMapping("/events/type/{eventType}")
    public Result<List<CalendarEventDto>> getEventsByType(@PathVariable Integer eventType) {
        Long userId = getCurrentUserId();
        
        List<DeveloperCalendarEvent> events = developerCalendarService.getEventsByType(eventType);
        List<CalendarEventDto> eventDtos = convertToEventDtos(events, userId);
        
        return Result.success(eventDtos);
    }
    
    /**
     * 获取重要事件列表
     */
    @Log(module = "摸鱼工具", type = Log.OperationType.SELECT, description = "获取重要事件")
    @GetMapping("/events/major")
    public Result<List<CalendarEventDto>> getMajorEvents() {
        Long userId = getCurrentUserId();
        
        List<DeveloperCalendarEvent> events = developerCalendarService.getMajorEvents();
        List<CalendarEventDto> eventDtos = convertToEventDtos(events, userId);
        
        return Result.success(eventDtos);
    }
    
    /**
     * 获取用户偏好设置
     */
    @Log(module = "摸鱼工具", type = Log.OperationType.SELECT, description = "获取用户偏好设置")
    @GetMapping("/preference")
    public Result<UserCalendarPreference> getUserPreference() {
        Long userId = getCurrentUserId();
        UserCalendarPreference preference = developerCalendarService.getUserPreference(userId);
        return Result.success(preference);
    }
    
    /**
     * 保存或更新用户偏好设置
     */
    @Log(module = "摸鱼工具", type = Log.OperationType.UPDATE, description = "保存用户偏好设置")
    @PostMapping("/preference")
    public Result<Void> saveUserPreference(@Valid @RequestBody CalendarPreferenceRequest request) {
        Long userId = getCurrentUserId();
        
        UserCalendarPreference preference = new UserCalendarPreference();
        preference.setEventReminder(request.getEventReminder());
        preference.setDailyContentPush(request.getDailyContentPush());
        preference.setPreferredLanguages(JsonUtils.listToJson(request.getPreferredLanguages()));
        preference.setPreferredContentTypes(JsonUtils.integerListToJson(request.getPreferredContentTypes()));
        preference.setDifficultyPreference(request.getDifficultyPreference());
        preference.setNotificationTime(request.getNotificationTime());
        
        boolean success = developerCalendarService.saveOrUpdateUserPreference(userId, preference);
        if (!success) {
            throw new BusinessException("保存偏好设置失败");
        }
        
        return Result.success();
    }
    
    /**
     * 切换事件收藏状态
     */
    @Log(module = "摸鱼工具", type = Log.OperationType.UPDATE, description = "切换事件收藏状态")
    @PostMapping("/events/{eventId}/toggle-collection")
    public Result<Void> toggleEventCollection(@PathVariable Long eventId) {
        Long userId = getCurrentUserId();
        
        boolean success = developerCalendarService.toggleEventCollection(userId, eventId);
        if (!success) {
            throw new BusinessException("操作失败");
        }
        
        return Result.success();
    }
    
    /**
     * 获取用户收藏的事件列表
     */
    @Log(module = "摸鱼工具", type = Log.OperationType.SELECT, description = "获取收藏事件")
    @GetMapping("/collections/events")
    public Result<List<CalendarEventDto>> getUserCollectedEvents() {
        Long userId = getCurrentUserId();
        
        List<DeveloperCalendarEvent> events = developerCalendarService.getUserCollectedEvents(userId);
        List<CalendarEventDto> eventDtos = convertToEventDtos(events, userId);
        
        return Result.success(eventDtos);
    }
    
    /**
     * 获取当前用户ID
     */
    private Long getCurrentUserId() {
        Long userId = userContextUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        return userId;
    }
    
    /**
     * 转换为事件DTO列表
     */
    private List<CalendarEventDto> convertToEventDtos(List<DeveloperCalendarEvent> events, Long userId) {
        return events.stream()
                .map(event -> convertToEventDto(event, userId))
                .collect(Collectors.toList());
    }
    
    /**
     * 转换为事件DTO
     */
    private CalendarEventDto convertToEventDto(DeveloperCalendarEvent event, Long userId) {
        CalendarEventDto dto = new CalendarEventDto();
        dto.setId(event.getId());
        dto.setEventName(event.getEventName());
        dto.setEventDate(event.getEventDate());
        dto.setEventType(event.getEventType());
        dto.setEventTypeName(getEventTypeName(event.getEventType()));
        dto.setDescription(event.getDescription());
        dto.setIcon(event.getIcon());
        dto.setColor(event.getColor());
        dto.setIsMajor(event.getIsMajor() == 1);
        dto.setBlessingText(event.getBlessingText());
        dto.setRelatedLinks(JsonUtils.jsonToStringList(event.getRelatedLinks()));
        dto.setIsCollected(developerCalendarService.isEventCollected(userId, event.getId()));
        
        return dto;
    }
    
    /**
     * 获取事件类型名称
     */
    private String getEventTypeName(Integer eventType) {
        switch (eventType) {
            case 1: return "程序员节日";
            case 2: return "技术纪念日";
            case 3: return "开源节日";
            default: return "未知类型";
        }
    }
}
