package com.xiaou.activity.service;

import com.xiaou.activity.domain.resp.UserPointsResp;
import com.xiaou.common.domain.R;

import java.util.List;

/**
 * 用户积分Service接口
 */
public interface UserPointsService {

    /**
     * 获取用户积分余额列表（按积分类型分组）
     * @param userId 用户ID
     * @return 用户积分余额列表
     */
    R<List<UserPointsResp>> getUserPointsBalance(String userId);

    /**
     * 获取当前用户积分余额列表
     * @return 用户积分余额列表
     */
    R<List<UserPointsResp>> getCurrentUserPointsBalance();

    /**
     * 增加用户积分
     * @param userId 用户ID
     * @param pointsTypeId 积分类型ID
     * @param amount 积分数量
     * @return 操作结果
     */
    R<Void> addUserPoints(String userId, Long pointsTypeId, Integer amount);

    /**
     * 扣减用户积分
     * @param userId 用户ID
     * @param pointsTypeId 积分类型ID
     * @param amount 积分数量
     * @return 操作结果
     */
    R<Void> subtractUserPoints(String userId, Long pointsTypeId, Integer amount);

    /**
     * 管理员调整用户积分余额
     * @param userId 用户ID
     * @param pointsTypeId 积分类型ID
     * @param amount 调整数量（正数增加，负数减少）
     * @param reason 调整原因
     * @return 操作结果
     */
    R<Void> adjustUserPointsBalance(String userId, Long pointsTypeId, Integer amount, String reason);
} 