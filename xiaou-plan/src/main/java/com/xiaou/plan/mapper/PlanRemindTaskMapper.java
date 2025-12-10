package com.xiaou.plan.mapper;

import com.xiaou.plan.domain.PlanRemindTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 计划提醒任务Mapper接口
 * 
 * @author xiaou
 */
@Mapper
public interface PlanRemindTaskMapper {
    
    /**
     * 插入提醒任务
     */
    int insert(PlanRemindTask task);
    
    /**
     * 批量插入提醒任务
     */
    int batchInsert(@Param("tasks") List<PlanRemindTask> tasks);
    
    /**
     * 查询待发送的提醒任务
     */
    List<PlanRemindTask> selectPendingTasks(@Param("startTime") LocalDateTime startTime, 
                                             @Param("endTime") LocalDateTime endTime);
    
    /**
     * 更新任务状态
     */
    int updateStatus(@Param("id") Long id, 
                     @Param("status") Integer status, 
                     @Param("sendTime") LocalDateTime sendTime);
    
    /**
     * 取消计划的所有待发送任务
     */
    int cancelByPlanId(@Param("planId") Long planId);
    
    /**
     * 查询某天是否已生成提醒任务
     */
    int countByPlanIdAndDate(@Param("planId") Long planId, @Param("remindDate") LocalDate remindDate);
    
    /**
     * 删除某天之前的已发送任务（清理历史数据）
     */
    int deleteOldTasks(@Param("beforeDate") LocalDate beforeDate);
}
