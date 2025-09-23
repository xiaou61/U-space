package com.xiaou.moyu.service;

import com.xiaou.moyu.domain.DeveloperCalendarEvent;
import com.xiaou.moyu.domain.UserCalendarPreference;

import java.util.List;

/**
 * 程序员日历服务接口
 *
 * @author xiaou
 */
public interface DeveloperCalendarService {

    /**
     * 根据日期获取事件列表
     *
     * @param eventDate 事件日期(MM-dd格式)
     * @return 事件列表
     */
    List<DeveloperCalendarEvent> getEventsByDate(String eventDate);

    /**
     * 获取指定月份的所有事件
     *
     * @param month 月份(MM格式)
     * @return 事件列表
     */
    List<DeveloperCalendarEvent> getEventsByMonth(String month);

    /**
     * 获取重要节日列表
     *
     * @return 重要节日列表
     */
    List<DeveloperCalendarEvent> getMajorEvents();

    /**
     * 根据事件类型获取事件列表
     *
     * @param eventType 事件类型
     * @return 事件列表
     */
    List<DeveloperCalendarEvent> getEventsByType(Integer eventType);

    /**
     * 获取今日事件
     *
     * @return 今日事件列表
     */
    List<DeveloperCalendarEvent> getTodayEvents();

    /**
     * 获取用户日历偏好设置
     *
     * @param userId 用户ID
     * @return 偏好设置
     */
    UserCalendarPreference getUserPreference(Long userId);

    /**
     * 保存或更新用户偏好设置
     *
     * @param userId 用户ID
     * @param preference 偏好设置
     * @return 是否成功
     */
    boolean saveOrUpdateUserPreference(Long userId, UserCalendarPreference preference);

    /**
     * 切换事件收藏状态
     *
     * @param userId 用户ID
     * @param eventId 事件ID
     * @return 是否成功
     */
    boolean toggleEventCollection(Long userId, Long eventId);

    /**
     * 获取用户收藏的事件列表
     *
     * @param userId 用户ID
     * @return 收藏的事件列表
     */
    List<DeveloperCalendarEvent> getUserCollectedEvents(Long userId);

    /**
     * 检查用户是否收藏了指定事件
     *
     * @param userId 用户ID
     * @param eventId 事件ID
     * @return 是否已收藏
     */
    boolean isEventCollected(Long userId, Long eventId);

    // ========== 后台管理方法 ==========

    /**
     * 获取所有事件（后台管理）
     *
     * @return 所有事件列表
     */
    List<DeveloperCalendarEvent> getAllEvents();

    /**
     * 根据ID获取事件（后台管理）
     *
     * @param id 事件ID
     * @return 事件信息
     */
    DeveloperCalendarEvent getEventById(Long id);

    /**
     * 创建事件（后台管理）
     *
     * @param event 事件信息
     * @return 是否成功
     */
    boolean createEvent(DeveloperCalendarEvent event);

    /**
     * 更新事件（后台管理）
     *
     * @param event 事件信息
     * @return 是否成功
     */
    boolean updateEvent(DeveloperCalendarEvent event);

    /**
     * 删除事件（后台管理）
     *
     * @param id 事件ID
     * @return 是否成功
     */
    boolean deleteEvent(Long id);

    /**
     * 批量删除事件（后台管理）
     *
     * @param ids 事件ID列表
     * @return 是否成功
     */
    boolean batchDeleteEvents(List<Long> ids);

    /**
     * 更新事件状态（后台管理）
     *
     * @param id 事件ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateEventStatus(Long id, Integer status);

    /**
     * 获取事件统计信息（后台管理）
     *
     * @return 统计信息
     */
    Object getEventStatistics();
}
