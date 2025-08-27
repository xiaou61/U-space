package com.xiaou.activity.mapper;

import com.xiaou.activity.domain.entity.PointsRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

/**
 * 积分发放记录Mapper接口
 */
@Mapper
public interface PointsRecordMapper {

    /**
     * 插入积分记录
     * @param pointsRecord 积分记录
     * @return 影响行数
     */
    int insert(PointsRecord pointsRecord);

    /**
     * 根据ID查询积分记录
     * @param id 记录ID
     * @return 积分记录
     */
    PointsRecord selectById(@Param("id") Long id);

    /**
     * 根据用户ID和活动ID查询积分记录
     * @param userId 用户ID
     * @param activityId 活动ID
     * @return 积分记录
     */
    PointsRecord selectByUserAndActivity(@Param("userId") String userId, @Param("activityId") Long activityId);

    /**
     * 分页查询用户积分记录
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 积分记录列表
     */
    List<PointsRecord> selectByUserIdPage(@Param("userId") String userId, 
                                         @Param("offset") Integer offset, 
                                         @Param("limit") Integer limit);

    /**
     * 查询用户积分记录总数
     * @param userId 用户ID
     * @return 总数
     */
    Integer selectCountByUserId(@Param("userId") String userId);

    /**
     * 根据活动ID查询积分记录
     * @param activityId 活动ID
     * @return 积分记录列表
     */
    List<PointsRecord> selectByActivityId(@Param("activityId") Long activityId);

    /**
     * 根据批次号查询积分记录
     * @param batchNo 批次号
     * @return 积分记录列表
     */
    List<PointsRecord> selectByBatchNo(@Param("batchNo") String batchNo);

    /**
     * 更新积分发放状态
     * @param id 记录ID
     * @param status 状态
     * @param grantTime 发放时间
     * @param failReason 失败原因
     * @return 影响行数
     */
    int updateGrantStatus(@Param("id") Long id, 
                         @Param("status") Integer status, 
                         @Param("grantTime") Date grantTime,
                         @Param("failReason") String failReason);

    /**
     * 批量更新积分发放状态
     * @param ids 记录ID列表
     * @param status 状态
     * @param grantTime 发放时间
     * @return 影响行数
     */
    int batchUpdateGrantStatus(@Param("ids") List<Long> ids, 
                              @Param("status") Integer status, 
                              @Param("grantTime") Date grantTime);

    /**
     * 查询待发放的积分记录
     * @return 待发放记录列表
     */
    List<PointsRecord> selectPendingRecords();

    /**
     * 查询可撤销的积分记录
     * @param activityId 活动ID
     * @return 可撤销记录列表
     */
    List<PointsRecord> selectRevokableRecords(@Param("activityId") Long activityId);

    /**
     * 根据积分类型ID查询相关记录
     * @param pointsTypeId 积分类型ID
     * @return 相关记录列表
     */
    List<PointsRecord> selectByPointsTypeId(@Param("pointsTypeId") Long pointsTypeId);

    /**
     * 分页查询积分记录
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 积分记录列表
     */
    List<PointsRecord> selectPage(@Param("offset") Integer offset, @Param("limit") Integer limit);

    /**
     * 查询积分记录总数
     * @return 总数
     */
    Integer selectCount();
} 