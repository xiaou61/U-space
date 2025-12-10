package com.xiaou.plan.service;

import com.xiaou.common.core.domain.PageResult;
import com.xiaou.plan.domain.PlanCheckinRecord;
import com.xiaou.plan.dto.*;

import java.util.List;

/**
 * 计划服务接口
 * 
 * @author xiaou
 */
public interface PlanService {
    
    /**
     * 创建计划
     */
    PlanResponse createPlan(Long userId, PlanCreateRequest request);
    
    /**
     * 更新计划
     */
    PlanResponse updatePlan(Long userId, Long planId, PlanCreateRequest request);
    
    /**
     * 删除计划
     */
    boolean deletePlan(Long userId, Long planId);
    
    /**
     * 获取计划详情
     */
    PlanResponse getPlanDetail(Long userId, Long planId);
    
    /**
     * 获取计划列表
     */
    PageResult<PlanResponse> getPlanList(PlanListRequest request);
    
    /**
     * 暂停计划
     */
    boolean pausePlan(Long userId, Long planId);
    
    /**
     * 恢复计划
     */
    boolean resumePlan(Long userId, Long planId);
    
    /**
     * 获取今日任务列表
     */
    List<TodayTaskResponse> getTodayTasks(Long userId);
    
    /**
     * 执行打卡
     */
    PlanCheckinResponse checkin(Long userId, PlanCheckinRequest request);
    
    /**
     * 获取计划打卡记录
     */
    List<PlanCheckinRecord> getCheckinRecords(Long userId, Long planId);
    
    /**
     * 获取统计概览
     */
    PlanStatsResponse getStatsOverview(Long userId);
}
