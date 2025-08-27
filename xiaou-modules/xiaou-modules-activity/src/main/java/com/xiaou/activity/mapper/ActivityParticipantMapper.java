package com.xiaou.activity.mapper;

import com.xiaou.activity.domain.entity.ActivityParticipant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 活动参与记录Mapper接口
 */
@Mapper
public interface ActivityParticipantMapper {

    /**
     * 插入参与记录
     * @param participant 参与记录
     * @return 影响行数
     */
    int insert(ActivityParticipant participant);

    /**
     * 根据活动ID和用户ID查询参与记录
     * @param activityId 活动ID
     * @param userId 用户ID
     * @return 参与记录
     */
    ActivityParticipant selectByActivityAndUser(@Param("activityId") Long activityId, @Param("userId") String userId);

    /**
     * 更新参与状态
     * @param id 记录ID
     * @param status 状态
     * @return 影响行数
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 分页查询活动参与者列表
     * @param activityId 活动ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 参与者列表
     */
    List<ActivityParticipant> selectParticipantsByActivity(@Param("activityId") Long activityId, 
                                                          @Param("offset") Integer offset, 
                                                          @Param("limit") Integer limit);

    /**
     * 查询活动参与者总数
     * @param activityId 活动ID
     * @return 参与者总数
     */
    Integer selectParticipantsCountByActivity(@Param("activityId") Long activityId);

    /**
     * 查询用户参与的活动列表
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 参与记录列表
     */
    List<ActivityParticipant> selectUserParticipatedActivities(@Param("userId") String userId,
                                                              @Param("offset") Integer offset,
                                                              @Param("limit") Integer limit);

    /**
     * 查询用户参与的活动总数
     * @param userId 用户ID
     * @return 总数
     */
    Integer selectUserParticipatedActivitiesCount(@Param("userId") String userId);

    /**
     * 获取用户在活动中的排名
     * @param activityId 活动ID
     * @param userId 用户ID
     * @return 排名
     */
    Integer getUserRankInActivity(@Param("activityId") Long activityId, @Param("userId") String userId);

    /**
     * 获取活动参与人数
     * @param activityId 活动ID
     * @return 参与人数
     */
    Integer getParticipantCount(@Param("activityId") Long activityId);

    /**
     * 查询活动的成功参与者列表（用于积分发放）
     * @param activityId 活动ID
     * @return 成功参与者列表
     */
    List<ActivityParticipant> selectSuccessfulParticipants(@Param("activityId") Long activityId);
} 