package com.xiaou.team.service;

import com.xiaou.team.dto.TaskCreateRequest;
import com.xiaou.team.dto.TaskResponse;

import java.util.List;

/**
 * 打卡任务服务接口
 * 
 * @author xiaou
 */
public interface TeamTaskService {
    
    /**
     * 创建任务
     * 
     * @param teamId 小组ID
     * @param request 创建请求
     * @param userId 创建人ID
     * @return 任务ID
     */
    Long createTask(Long teamId, TaskCreateRequest request, Long userId);
    
    /**
     * 更新任务
     * 
     * @param taskId 任务ID
     * @param request 更新请求
     * @param userId 操作人ID
     */
    void updateTask(Long taskId, TaskCreateRequest request, Long userId);
    
    /**
     * 删除任务
     * 
     * @param taskId 任务ID
     * @param userId 操作人ID
     */
    void deleteTask(Long taskId, Long userId);
    
    /**
     * 启用/禁用任务
     * 
     * @param taskId 任务ID
     * @param status 状态：0禁用 1启用
     * @param userId 操作人ID
     */
    void setTaskStatus(Long taskId, Integer status, Long userId);
    
    /**
     * 获取任务详情
     * 
     * @param taskId 任务ID
     * @param userId 当前用户ID（用于检查打卡状态）
     * @return 任务详情
     */
    TaskResponse getTaskDetail(Long taskId, Long userId);
    
    /**
     * 获取小组任务列表
     * 
     * @param teamId 小组ID
     * @param status 状态（可选）
     * @param userId 当前用户ID
     * @return 任务列表
     */
    List<TaskResponse> getTaskList(Long teamId, Integer status, Long userId);
    
    /**
     * 获取今日需要打卡的任务
     * 
     * @param teamId 小组ID
     * @param userId 用户ID
     * @return 今日任务列表
     */
    List<TaskResponse> getTodayTasks(Long teamId, Long userId);
}
