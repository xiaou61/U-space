package com.xiaou.points.mapper;

import com.xiaou.points.domain.UserPointsDetail;
import com.xiaou.points.dto.PointsDetailQueryRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户积分明细Mapper接口
 * 
 * @author xiaou
 */
@Mapper
public interface UserPointsDetailMapper {
    
    /**
     * 插入积分明细记录
     */
    int insert(UserPointsDetail userPointsDetail);
    
    /**
     * 根据ID查询明细
     */
    UserPointsDetail selectById(@Param("id") Long id);
    
    /**
     * 查询用户积分明细列表（分页）
     */
    List<UserPointsDetail> selectDetailList(PointsDetailQueryRequest request);
    
    /**
     * 查询所有积分明细列表（管理端分页）
     */
    List<UserPointsDetail> selectAllDetailList(PointsDetailQueryRequest request);
    
    /**
     * 统计用户积分明细总数
     */
    Long countUserDetails(@Param("userId") Long userId, @Param("pointsType") Integer pointsType);
    
    /**
     * 统计所有积分明细总数
     */
    Long countAllDetails(@Param("pointsType") Integer pointsType);
    
    /**
     * 统计指定类型积分总量
     */
    Long selectPointsSumByType(@Param("pointsType") Integer pointsType);
    
    /**
     * 查询用户今日积分明细
     */
    List<UserPointsDetail> selectTodayDetails(@Param("userId") Long userId);
    
    /**
     * 查询用户指定时间范围的积分明细
     */
    List<UserPointsDetail> selectDetailsByTimeRange(@Param("userId") Long userId, 
                                                   @Param("startTime") String startTime, 
                                                   @Param("endTime") String endTime);
}
