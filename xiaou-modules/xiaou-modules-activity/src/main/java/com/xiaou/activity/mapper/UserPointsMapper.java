package com.xiaou.activity.mapper;

import com.xiaou.activity.domain.entity.UserPoints;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户积分余额Mapper接口
 */
@Mapper
public interface UserPointsMapper {

    /**
     * 插入积分记录
     * @param userPoints 积分记录
     * @return 影响行数
     */
    int insert(UserPoints userPoints);

    /**
     * 根据用户ID和积分类型ID查询积分余额
     * @param userId 用户ID
     * @param pointsTypeId 积分类型ID
     * @return 积分余额记录
     */
    UserPoints selectByUserAndPointsType(@Param("userId") String userId, @Param("pointsTypeId") Long pointsTypeId);

    /**
     * 查询用户所有积分余额
     * @param userId 用户ID
     * @return 积分余额列表
     */
    List<UserPoints> selectByUserId(@Param("userId") String userId);

    /**
     * 增加积分余额
     * @param userId 用户ID
     * @param pointsTypeId 积分类型ID
     * @param amount 增加数量
     * @return 影响行数
     */
    int addPoints(@Param("userId") String userId, @Param("pointsTypeId") Long pointsTypeId, @Param("amount") Integer amount);

    /**
     * 扣减积分余额
     * @param userId 用户ID
     * @param pointsTypeId 积分类型ID
     * @param amount 扣减数量
     * @return 影响行数
     */
    int deductPoints(@Param("userId") String userId, @Param("pointsTypeId") Long pointsTypeId, @Param("amount") Integer amount);

    /**
     * 增加冻结积分
     * @param userId 用户ID
     * @param pointsTypeId 积分类型ID
     * @param amount 冻结数量
     * @return 影响行数
     */
    int addFrozenPoints(@Param("userId") String userId, @Param("pointsTypeId") Long pointsTypeId, @Param("amount") Integer amount);

    /**
     * 减少冻结积分（解冻）
     * @param userId 用户ID
     * @param pointsTypeId 积分类型ID
     * @param amount 解冻数量
     * @return 影响行数
     */
    int reduceFrozenPoints(@Param("userId") String userId, @Param("pointsTypeId") Long pointsTypeId, @Param("amount") Integer amount);

    /**
     * 冻结积分转为正式积分
     * @param userId 用户ID
     * @param pointsTypeId 积分类型ID
     * @param amount 数量
     * @return 影响行数
     */
    int convertFrozenToBalance(@Param("userId") String userId, @Param("pointsTypeId") Long pointsTypeId, @Param("amount") Integer amount);

    /**
     * 获取用户指定类型积分余额
     * @param userId 用户ID
     * @param pointsTypeId 积分类型ID
     * @return 积分余额，如果没有记录返回0
     */
    Integer getUserPointsBalance(@Param("userId") String userId, @Param("pointsTypeId") Long pointsTypeId);

    /**
     * 根据积分类型ID查询用户积分记录
     * @param pointsTypeId 积分类型ID
     * @return 用户积分记录列表
     */
    List<UserPoints> selectByPointsTypeId(@Param("pointsTypeId") Long pointsTypeId);
} 