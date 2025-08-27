package com.xiaou.activity.service.impl;

import com.xiaou.activity.domain.entity.Activity;
import com.xiaou.activity.domain.entity.PointsRecord;
import com.xiaou.activity.domain.entity.PointsType;
import com.xiaou.activity.domain.req.PointsGrantReq;
import com.xiaou.activity.domain.resp.PointsRecordResp;
import com.xiaou.activity.mapper.ActivityMapper;
import com.xiaou.activity.mapper.ActivityParticipantMapper;
import com.xiaou.activity.mapper.PointsRecordMapper;
import com.xiaou.activity.mapper.PointsTypeMapper;
import com.xiaou.activity.service.PointsRecordService;
import com.xiaou.activity.service.UserPointsService;
import com.xiaou.activity.service.UserNameService;
import com.xiaou.activity.utils.ActivityStatusUtil;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;

import com.xiaou.satoken.utils.LoginHelper;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 积分发放记录Service业务层处理
 */
@Service
public class PointsRecordServiceImpl implements PointsRecordService {

    @Resource
    private PointsRecordMapper pointsRecordMapper;

    @Resource
    private ActivityMapper activityMapper;

    @Resource
    private PointsTypeMapper pointsTypeMapper;

    @Resource
    private UserPointsService userPointsService;

    @Resource
    private ActivityParticipantMapper activityParticipantMapper;

    @Resource
    private UserNameService userNameService;
    @Autowired
    private LoginHelper loginHelper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> grantPointsManually(PointsGrantReq req) {
        try {
            // 验证积分类型
            PointsType pointsType = pointsTypeMapper.selectById(req.getPointsTypeId());
            if (pointsType == null || pointsType.getIsActive() == 0) {
                return R.fail("积分类型不存在或已禁用");
            }

            // 验证活动（如果指定）
            Activity activity = null;
            if (req.getActivityId() != null) {
                activity = activityMapper.selectById(req.getActivityId());
                if (activity == null) {
                    return R.fail("活动不存在");
                }
            }

            // 生成批次号
            String batchNo = "MANUAL_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);

            // 为每个用户创建积分记录并发放
            for (String userId : req.getUserIds()) {
                // 检查是否已经存在发放记录（避免重复发放）
                if (req.getActivityId() != null) {
                    PointsRecord existingRecord = pointsRecordMapper.selectByUserAndActivity(userId, req.getActivityId());
                    if (existingRecord != null) {
                        continue; // 跳过已发放的用户
                    }
                }

                // 创建积分记录
                PointsRecord record = new PointsRecord();
                record.setUserId(userId);
                record.setActivityId(req.getActivityId());
                record.setPointsTypeId(req.getPointsTypeId());
                record.setPointsAmount(req.getPointsAmount());
                record.setOperationType(1); // 获得积分
                record.setStatus(0); // 待发放
                record.setBatchNo(batchNo);

                int insertResult = pointsRecordMapper.insert(record);
                if (insertResult > 0) {
                    // 发放积分
                    R<Void> grantResult = userPointsService.addUserPoints(userId, req.getPointsTypeId(), req.getPointsAmount());
                    if (R.isSuccess(grantResult)) {
                        // 更新发放状态
                        pointsRecordMapper.updateGrantStatus(record.getId(), 1, new Date(), null);
                    } else {
                        // 发放失败，更新失败状态
                        pointsRecordMapper.updateGrantStatus(record.getId(), 2, null, grantResult.getMsg());
                    }
                }
            }

            return R.ok();
        } catch (Exception e) {
            return R.fail("批量发放积分失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> batchGrantPointsForActivity(Long activityId) {
        try {
            // 检查活动是否存在
            Activity activity = activityMapper.selectById(activityId);
            if (activity == null) {
                return R.fail("活动不存在");
            }

            // 检查活动是否已结束（实时判断）
            if (!ActivityStatusUtil.isFinished(activity)) {
                Integer status = ActivityStatusUtil.calculateStatus(activity);
                return R.fail("活动当前状态为【" + ActivityStatusUtil.getStatusName(status) + "】，只有已结束的活动才能发放积分");
            }

            // 获取成功参与的用户列表
            List<com.xiaou.activity.domain.entity.ActivityParticipant> successfulParticipants = 
                activityParticipantMapper.selectSuccessfulParticipants(activityId);
            if (successfulParticipants.isEmpty()) {
                return R.fail("没有成功参与的用户");
            }

            // 创建积分发放请求
            PointsGrantReq grantReq = new PointsGrantReq();
            grantReq.setActivityId(activityId);
            grantReq.setPointsTypeId(activity.getPointsTypeId());
            grantReq.setPointsAmount(activity.getPointsAmount());
            grantReq.setUserIds(successfulParticipants.stream()
                    .map(com.xiaou.activity.domain.entity.ActivityParticipant::getUserId)
                    .collect(Collectors.toList()));

            // 批量发放积分
            return grantPointsManually(grantReq);
        } catch (Exception e) {
            return R.fail("活动积分发放失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> revokePointsRecord(Long recordId) {
        try {
            // 查询积分记录
            PointsRecord record = pointsRecordMapper.selectById(recordId);
            if (record == null) {
                return R.fail("积分记录不存在");
            }

            // 检查是否可以撤销
            if (record.getStatus() != 1) {
                return R.fail("只有已发放的积分才能撤销");
            }

            // 撤销用户积分
            R<Void> deductResult = userPointsService.subtractUserPoints(
                    record.getUserId(), 
                    record.getPointsTypeId(), 
                    record.getPointsAmount()
            );

            if (R.isSuccess(deductResult)) {
                // 更新记录状态为已撤销
                pointsRecordMapper.updateGrantStatus(record.getId(), 3, null, "管理员撤销");
                return R.ok();
            } else {
                return R.fail("撤销积分失败：" + deductResult.getMsg());
            }

        } catch (Exception e) {
            return R.fail("撤销积分发放失败：" + e.getMessage());
        }
    }

    @Override
    public R<PageRespDto<PointsRecordResp>> pagePointsRecords(PageReqDto req) {
        try {
            // 计算分页参数
            int offset = (req.getPageNum() - 1) * req.getPageSize();
            
            // 查询积分记录
            List<PointsRecord> records = pointsRecordMapper.selectPage(offset, req.getPageSize());
            Integer total = pointsRecordMapper.selectCount();

            // 转换为响应对象
            List<PointsRecordResp> respList = records.stream()
                    .map(this::convertToPointsRecordResp)
                    .collect(Collectors.toList());

            PageRespDto<PointsRecordResp> pageResp = PageRespDto.of(req.getPageNum(), req.getPageSize(), total.longValue(), respList);
            return R.ok(pageResp);
        } catch (Exception e) {
            return R.fail("查询积分记录失败：" + e.getMessage());
        }
    }

    @Override
    public R<PageRespDto<PointsRecordResp>> pageUserPointsRecords(String userId, PageReqDto req) {
        try {
            int offset = (req.getPageNum() - 1) * req.getPageSize();
            
            // 查询指定用户的积分记录
            List<PointsRecord> records = pointsRecordMapper.selectByUserIdPage(userId, offset, req.getPageSize());
            Integer total = pointsRecordMapper.selectCountByUserId(userId);

            // 转换为响应对象
            List<PointsRecordResp> respList = records.stream()
                    .map(this::convertToPointsRecordResp)
                    .collect(Collectors.toList());

            PageRespDto<PointsRecordResp> pageResp = PageRespDto.of(req.getPageNum(), req.getPageSize(), total.longValue(), respList);
            return R.ok(pageResp);
        } catch (Exception e) {
            return R.fail("查询用户积分记录失败：" + e.getMessage());
        }
    }

    @Override
    public R<PageRespDto<PointsRecordResp>> pageCurrentUserPointsRecords(PageReqDto req) {
        try {
            String currentUserId = loginHelper.getCurrentAppUserId();
            return pageUserPointsRecords(currentUserId, req);
        } catch (Exception e) {
            return R.fail("查询当前用户积分记录失败：" + e.getMessage());
        }
    }

    /**
     * 转换PointsRecord实体为PointsRecordResp
     */
    private PointsRecordResp convertToPointsRecordResp(PointsRecord record) {
        PointsRecordResp resp = new PointsRecordResp();
        BeanUtils.copyProperties(record, resp);

        // 设置积分类型信息
        PointsType pointsType = pointsTypeMapper.selectById(record.getPointsTypeId());
        if (pointsType != null) {
            resp.setPointsTypeName(pointsType.getTypeName());
        }

        // 设置活动信息
        if (record.getActivityId() != null) {
            Activity activity = activityMapper.selectById(record.getActivityId());
            if (activity != null) {
                resp.setActivityTitle(activity.getTitle());
            }
        }

        // 设置操作类型名称
        resp.setOperationTypeName(getOperationTypeName(record.getOperationType()));

        // 设置状态名称
        resp.setStatusName(getStatusName(record.getStatus()));

        // 设置用户名称
        resp.setUserName(userNameService.getUserNameById(record.getUserId()));

        return resp;
    }

    /**
     * 获取操作类型名称
     */
    private String getOperationTypeName(Integer operationType) {
        if (operationType == null) return "";
        switch (operationType) {
            case 1: return "获得";
            case 2: return "扣除";
            default: return "未知";
        }
    }

    /**
     * 获取状态名称
     */
    private String getStatusName(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 0: return "待发放";
            case 1: return "已发放";
            case 2: return "发放失败";
            case 3: return "已撤销";
            default: return "未知";
        }
    }
} 