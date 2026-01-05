package com.xiaou.team.service;

import com.xiaou.team.dto.CheckinRequest;
import com.xiaou.team.dto.CheckinResponse;

import java.time.LocalDate;
import java.util.List;

/**
 * 打卡服务接口
 * 
 * @author xiaou
 */
public interface TeamCheckinService {
    
    /**
     * 打卡
     * 
     * @param teamId 小组ID
     * @param request 打卡请求
     * @param userId 用户ID
     * @return 打卡ID
     */
    Long checkin(Long teamId, CheckinRequest request, Long userId);
    
    /**
     * 补卡
     * 
     * @param teamId 小组ID
     * @param request 打卡请求
     * @param date 补卡日期
     * @param userId 用户ID
     * @return 打卡ID
     */
    Long supplementCheckin(Long teamId, CheckinRequest request, LocalDate date, Long userId);
    
    /**
     * 删除打卡记录
     * 
     * @param checkinId 打卡ID
     * @param userId 用户ID
     */
    void deleteCheckin(Long checkinId, Long userId);
    
    /**
     * 获取打卡详情
     * 
     * @param checkinId 打卡ID
     * @param userId 当前用户ID（用于检查点赞状态）
     * @return 打卡详情
     */
    CheckinResponse getCheckinDetail(Long checkinId, Long userId);
    
    /**
     * 获取小组打卡动态列表
     * 
     * @param teamId 小组ID
     * @param taskId 任务ID（可选）
     * @param page 页码
     * @param pageSize 每页条数
     * @param userId 当前用户ID
     * @return 打卡列表
     */
    List<CheckinResponse> getCheckinList(Long teamId, Long taskId, Integer page, Integer pageSize, Long userId);
    
    /**
     * 获取用户打卡记录
     * 
     * @param userId 用户ID
     * @param teamId 小组ID（可选）
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 打卡列表
     */
    List<CheckinResponse> getUserCheckins(Long userId, Long teamId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 获取用户打卡日历数据
     * 
     * @param userId 用户ID
     * @param teamId 小组ID
     * @param year 年
     * @param month 月
     * @return 打卡日期列表
     */
    List<LocalDate> getCheckinCalendar(Long userId, Long teamId, Integer year, Integer month);
    
    /**
     * 点赞打卡
     * 
     * @param checkinId 打卡ID
     * @param userId 用户ID
     */
    void likeCheckin(Long checkinId, Long userId);
    
    /**
     * 取消点赞
     * 
     * @param checkinId 打卡ID
     * @param userId 用户ID
     */
    void unlikeCheckin(Long checkinId, Long userId);
    
    /**
     * 获取用户连续打卡天数
     * 
     * @param userId 用户ID
     * @param teamId 小组ID
     * @param taskId 任务ID（可选）
     * @return 连续天数
     */
    Integer getStreakDays(Long userId, Long teamId, Long taskId);
    
    /**
     * 获取用户总打卡天数
     * 
     * @param userId 用户ID
     * @param teamId 小组ID
     * @return 总打卡天数
     */
    Integer getTotalCheckinDays(Long userId, Long teamId);
}
