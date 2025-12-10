package com.xiaou.plan.mapper;

import com.xiaou.plan.domain.PlanCheckinRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 计划打卡记录Mapper接口
 * 
 * @author xiaou
 */
@Mapper
public interface PlanCheckinRecordMapper {
    
    /**
     * 插入打卡记录
     */
    int insert(PlanCheckinRecord record);
    
    /**
     * 查询某计划某日的打卡记录
     */
    PlanCheckinRecord selectByPlanIdAndDate(@Param("planId") Long planId, @Param("checkinDate") LocalDate checkinDate);
    
    /**
     * 查询某计划的打卡记录列表
     */
    List<PlanCheckinRecord> selectByPlanId(@Param("planId") Long planId);
    
    /**
     * 查询用户某日所有打卡记录
     */
    List<PlanCheckinRecord> selectByUserIdAndDate(@Param("userId") Long userId, @Param("checkinDate") LocalDate checkinDate);
    
    /**
     * 查询用户打卡记录列表
     */
    List<PlanCheckinRecord> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 统计计划打卡天数
     */
    int countByPlanId(@Param("planId") Long planId);
    
    /**
     * 查询最近的打卡记录
     */
    PlanCheckinRecord selectLatestByPlanId(@Param("planId") Long planId);
    
    /**
     * 查询用户某月的打卡记录
     */
    List<PlanCheckinRecord> selectByUserIdAndMonth(@Param("userId") Long userId, 
                                                    @Param("year") int year, 
                                                    @Param("month") int month);
    
    /**
     * 统计用户总打卡次数
     */
    int countByUserId(@Param("userId") Long userId);
}
