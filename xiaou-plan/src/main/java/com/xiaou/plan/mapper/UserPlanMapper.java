package com.xiaou.plan.mapper;

import com.xiaou.plan.domain.UserPlan;
import com.xiaou.plan.dto.PlanListRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 用户计划Mapper接口
 * 
 * @author xiaou
 */
@Mapper
public interface UserPlanMapper {
    
    /**
     * 插入计划
     */
    int insert(UserPlan plan);
    
    /**
     * 更新计划
     */
    int update(UserPlan plan);
    
    /**
     * 根据ID查询计划
     */
    UserPlan selectById(@Param("id") Long id);
    
    /**
     * 根据用户ID和计划ID查询
     */
    UserPlan selectByUserIdAndId(@Param("userId") Long userId, @Param("id") Long id);
    
    /**
     * 查询用户计划列表
     */
    List<UserPlan> selectList(PlanListRequest request);
    
    /**
     * 查询用户今日需要打卡的计划
     */
    List<UserPlan> selectTodayPlans(@Param("userId") Long userId, @Param("today") LocalDate today);
    
    /**
     * 查询用户所有进行中的计划
     */
    List<UserPlan> selectActivePlans(@Param("userId") Long userId);
    
    /**
     * 更新计划状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    
    /**
     * 更新打卡统计信息
     */
    int updateCheckinStats(@Param("id") Long id, 
                           @Param("totalCheckinDays") Integer totalCheckinDays,
                           @Param("currentStreak") Integer currentStreak,
                           @Param("maxStreak") Integer maxStreak);
    
    /**
     * 逻辑删除计划
     */
    int deleteById(@Param("id") Long id, @Param("userId") Long userId);
    
    /**
     * 查询需要生成提醒任务的计划
     */
    List<UserPlan> selectPlansForRemind(@Param("today") LocalDate today);
    
    /**
     * 统计用户计划数量
     */
    int countByUserId(@Param("userId") Long userId, @Param("status") Integer status);
}
