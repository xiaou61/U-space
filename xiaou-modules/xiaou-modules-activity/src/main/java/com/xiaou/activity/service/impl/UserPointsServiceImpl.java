package com.xiaou.activity.service.impl;

import com.xiaou.activity.domain.entity.PointsRecord;
import com.xiaou.activity.domain.entity.PointsType;
import com.xiaou.activity.domain.entity.UserPoints;
import com.xiaou.activity.domain.resp.UserPointsResp;
import com.xiaou.activity.mapper.PointsRecordMapper;
import com.xiaou.activity.mapper.PointsTypeMapper;
import com.xiaou.activity.mapper.UserPointsMapper;
import com.xiaou.activity.service.UserPointsService;
import com.xiaou.common.domain.R;

import com.xiaou.satoken.utils.LoginHelper;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户积分Service业务层处理
 */
@Service
public class UserPointsServiceImpl implements UserPointsService {

    @Resource
    private UserPointsMapper userPointsMapper;

    @Resource
    private PointsTypeMapper pointsTypeMapper;

    @Resource
    private PointsRecordMapper pointsRecordMapper;
    @Autowired
    private LoginHelper loginHelper;

    @Override
    public R<List<UserPointsResp>> getUserPointsBalance(String userId) {
        try {
            // 查询指定用户所有积分余额
            List<UserPoints> userPointsList = userPointsMapper.selectByUserId(userId);
            
            // 如果用户没有任何积分记录，返回所有积分类型的零余额
            if (userPointsList.isEmpty()) {
                // 查询所有启用的积分类型
                List<PointsType> activePointsTypes = pointsTypeMapper.selectActiveList();
                userPointsList = activePointsTypes.stream()
                        .map(pointsType -> {
                            UserPoints userPoints = new UserPoints();
                            userPoints.setUserId(userId);
                            userPoints.setPointsTypeId(pointsType.getId());
                            userPoints.setCurrentBalance(0);
                            userPoints.setTotalEarned(0);
                            userPoints.setTotalSpent(0);
                            userPoints.setFrozenPoints(0);
                            return userPoints;
                        })
                        .collect(Collectors.toList());
            }

            // 转换为响应对象
            List<UserPointsResp> respList = convertToUserPointsRespList(userPointsList);
            return R.ok(respList);
        } catch (Exception e) {
            return R.fail("查询用户积分余额失败：" + e.getMessage());
        }
    }

    @Override
    public R<List<UserPointsResp>> getCurrentUserPointsBalance() {
        try {
            String currentUserId = loginHelper.getCurrentAppUserId();
            return getUserPointsBalance(currentUserId);
        } catch (Exception e) {
            return R.fail("查询当前用户积分余额失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> addUserPoints(String userId, Long pointsTypeId, Integer amount) {
        try {
            // 检查积分类型是否存在且启用
            PointsType pointsType = pointsTypeMapper.selectById(pointsTypeId);
            if (pointsType == null || pointsType.getIsActive() == 0) {
                return R.fail("积分类型不存在或已禁用");
            }

            // 查找或创建用户积分记录
            UserPoints userPoints = userPointsMapper.selectByUserAndPointsType(userId, pointsTypeId);
            if (userPoints == null) {
                // 创建新记录
                userPoints = new UserPoints();
                userPoints.setUserId(userId);
                userPoints.setPointsTypeId(pointsTypeId);
                userPoints.setCurrentBalance(amount);
                userPoints.setTotalEarned(amount);
                userPoints.setTotalSpent(0);
                userPoints.setFrozenPoints(0);
                
                int insertResult = userPointsMapper.insert(userPoints);
                return insertResult > 0 ? R.ok() : R.fail("增加用户积分失败");
            } else {
                // 更新现有记录
                int updateResult = userPointsMapper.addPoints(userId, pointsTypeId, amount);
                return updateResult > 0 ? R.ok() : R.fail("增加用户积分失败");
            }
        } catch (Exception e) {
            return R.fail("增加用户积分失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> subtractUserPoints(String userId, Long pointsTypeId, Integer amount) {
        try {
            // 检查用户积分余额
            UserPoints userPoints = userPointsMapper.selectByUserAndPointsType(userId, pointsTypeId);
            if (userPoints == null || userPoints.getCurrentBalance() < amount) {
                return R.fail("用户积分余额不足");
            }

            // 扣减积分
            int updateResult = userPointsMapper.deductPoints(userId, pointsTypeId, amount);
            return updateResult > 0 ? R.ok() : R.fail("扣减用户积分失败");
        } catch (Exception e) {
            return R.fail("扣减用户积分失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> adjustUserPointsBalance(String userId, Long pointsTypeId, Integer amount, String reason) {
        try {
            // 检查积分类型是否存在且启用
            PointsType pointsType = pointsTypeMapper.selectById(pointsTypeId);
            if (pointsType == null || pointsType.getIsActive() == 0) {
                return R.fail("积分类型不存在或已禁用");
            }

            // 创建积分记录
            PointsRecord record = new PointsRecord();
            record.setUserId(userId);
            record.setPointsTypeId(pointsTypeId);
            record.setPointsAmount(Math.abs(amount));
            record.setOperationType(amount > 0 ? 1 : 2); // 1:获得 2:扣除
            record.setStatus(1); // 已发放
            record.setGrantTime(new java.util.Date());
            record.setBatchNo("ADMIN_ADJUST_" + System.currentTimeMillis());

            // 插入积分记录
            int insertResult = pointsRecordMapper.insert(record);
            if (insertResult == 0) {
                return R.fail("创建积分记录失败");
            }

            // 调整积分余额
            R<Void> adjustResult;
            if (amount > 0) {
                adjustResult = addUserPoints(userId, pointsTypeId, amount);
            } else {
                adjustResult = subtractUserPoints(userId, pointsTypeId, Math.abs(amount));
            }

            return R.isSuccess(adjustResult) ? R.ok() : adjustResult;
        } catch (Exception e) {
            return R.fail("调整用户积分余额失败：" + e.getMessage());
        }
    }

    /**
     * 转换为UserPointsResp列表
     */
    private List<UserPointsResp> convertToUserPointsRespList(List<UserPoints> userPointsList) {
        List<UserPointsResp> respList = new ArrayList<>();
        for (UserPoints userPoints : userPointsList) {
            UserPointsResp resp = new UserPointsResp();
            BeanUtils.copyProperties(userPoints, resp);
            
            // 查询积分类型信息
            PointsType pointsType = pointsTypeMapper.selectById(userPoints.getPointsTypeId());
            if (pointsType != null) {
                resp.setPointsTypeName(pointsType.getTypeName());
                resp.setPointsTypeCode(pointsType.getTypeCode());
                // 注意：如果UserPointsResp没有iconUrl字段，就先注释掉
                // resp.setIconUrl(pointsType.getIconUrl());
            }
            
            respList.add(resp);
        }
        return respList;
    }
} 