package com.xiaou.activity.mapper;

import com.xiaou.activity.domain.entity.Activity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 活动Mapper接口
 */
@Mapper
public interface ActivityMapper {

    /**
     * 插入活动
     * @param activity 活动信息
     * @return 影响行数
     */
    int insert(Activity activity);

    /**
     * 根据ID查询活动
     * @param id 活动ID
     * @return 活动信息
     */
    Activity selectById(@Param("id") Long id);

    /**
     * 更新活动
     * @param activity 活动信息
     * @return 影响行数
     */
    int updateById(Activity activity);

    /**
     * 根据ID删除活动（逻辑删除）
     * @param id 活动ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 分页查询活动列表
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 活动列表
     */
    List<Activity> selectPage(@Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 查询活动总数
     * @return 总数
     */
    Integer selectCount();

    /**
     * 查询可参与的活动列表
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 活动列表
     */
    List<Activity> selectAvailableActivities(@Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 查询可参与的活动总数
     * @return 总数
     */
    Integer selectAvailableActivitiesCount();

    /**
     * 增加活动参与人数
     * @param activityId 活动ID
     * @return 影响行数
     */
    int increaseParticipantCount(@Param("activityId") Long activityId);

    /**
     * 减少活动参与人数
     * @param activityId 活动ID
     * @return 影响行数
     */
    int decreaseParticipantCount(@Param("activityId") Long activityId);

    /**
     * 获取活动当前参与人数
     * @param activityId 活动ID
     * @return 当前参与人数
     */
    Integer getCurrentParticipantCount(@Param("activityId") Long activityId);

    /**
     * 更新活动状态
     * @param id 活动ID
     * @param status 状态
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 查询已结束但未发放积分的活动列表
     * @return 活动列表
     */
    List<Activity> selectFinishedActivitiesWithoutPoints();

    /**
     * 更新活动积分发放状态
     * @param id 活动ID
     * @param pointsGranted 积分发放状态
     * @return 影响行数
     */
    int updatePointsGranted(@Param("id") Long id, @Param("pointsGranted") Integer pointsGranted);

    /**
     * 查询需要状态更新的有效活动列表
     * 排除已取消(3)和已禁用(4)的活动
     * @return 活动列表
     */
    List<Activity> selectValidActivitiesForStatusUpdate();
} 