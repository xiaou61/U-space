package com.xiaou.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaou.activity.domain.entity.Activity;
import com.xiaou.activity.domain.entity.ActivityParticipant;
import com.xiaou.activity.domain.entity.PointsRecord;
import com.xiaou.activity.domain.entity.PointsType;
import com.xiaou.activity.domain.entity.UserPoints;
import com.xiaou.activity.domain.req.ActivityCreateReq;
import com.xiaou.activity.domain.req.ActivityUpdateReq;
import com.xiaou.activity.domain.resp.ActivityResp;
import com.xiaou.activity.domain.resp.ActivityParticipantResp;
import com.xiaou.activity.mapper.ActivityMapper;
import com.xiaou.activity.mapper.ActivityParticipantMapper;
import com.xiaou.activity.mapper.PointsRecordMapper;
import com.xiaou.activity.mapper.PointsTypeMapper;
import com.xiaou.activity.mapper.UserPointsMapper;
import com.xiaou.activity.service.ActivityCacheService;
import com.xiaou.activity.service.ActivityService;
import com.xiaou.activity.service.UserNameService;
import com.xiaou.activity.utils.ActivityStatusUtil;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.common.utils.QueryWrapperUtil;
import com.xiaou.satoken.utils.LoginHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 活动Service实现类
 */
@Service
@Slf4j
public class ActivityServiceImpl implements ActivityService {

    @Resource
    private ActivityMapper activityMapper;

    @Resource
    private PointsTypeMapper pointsTypeMapper;

    @Resource
    private ActivityParticipantMapper activityParticipantMapper;

    @Resource
    private PointsRecordMapper pointsRecordMapper;

    @Resource
    private UserPointsMapper userPointsMapper;

    @Resource
    private UserNameService userNameService;
    
    @Resource
    private ActivityCacheService activityCacheService;
    
    @Autowired
    private LoginHelper loginHelper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Long> createActivity(ActivityCreateReq req) {
        try {
            // 验证积分类型是否存在
            PointsType pointsType = pointsTypeMapper.selectById(req.getPointsTypeId());
            if (pointsType == null || pointsType.getIsActive() == 0) {
                return R.fail("积分类型不存在或已禁用");
            }

            // 验证时间逻辑
            if (req.getStartTime().after(req.getEndTime())) {
                return R.fail("活动开始时间不能晚于结束时间");
            }

            // 创建活动实体
            Activity activity = new Activity();
            BeanUtils.copyProperties(req, activity);
            activity.setCurrentParticipants(0);
            activity.setCreateUserId(loginHelper.getCurrentAppUserId());
            
            // 根据时间自动计算初始状态
            Integer calculatedStatus = ActivityStatusUtil.calculateStatus(activity);
            activity.setStatus(calculatedStatus);

            // 插入数据库
            int result = activityMapper.insert(activity);
            if (result > 0) {
                return R.ok(activity.getId());
            } else {
                return R.fail("创建活动失败");
            }
        } catch (Exception e) {
            return R.fail("创建活动失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> updateActivity(ActivityUpdateReq req) {
        try {
            // 检查活动是否存在
            Activity existingActivity = activityMapper.selectById(req.getId());
            if (existingActivity == null) {
                return R.fail("活动不存在");
            }

            // 检查活动是否可以修改（使用工具类实时判断）
            if (!ActivityStatusUtil.canEdit(existingActivity)) {
                Integer status = ActivityStatusUtil.calculateStatus(existingActivity);
                return R.fail("活动当前状态为【" + ActivityStatusUtil.getStatusName(status) + "】，不能修改");
            }

            // 验证时间逻辑
            Date startTime = req.getStartTime() != null ? req.getStartTime() : existingActivity.getStartTime();
            Date endTime = req.getEndTime() != null ? req.getEndTime() : existingActivity.getEndTime();
            if (startTime.after(endTime)) {
                return R.fail("活动开始时间不能晚于结束时间");
            }

            // 验证积分类型
            if (req.getPointsTypeId() != null) {
                PointsType pointsType = pointsTypeMapper.selectById(req.getPointsTypeId());
                if (pointsType == null || pointsType.getIsActive() == 0) {
                    return R.fail("积分类型不存在或已禁用");
                }
            }

            // 验证参与人数限制
            if (req.getMaxParticipants() != null && req.getMaxParticipants() < existingActivity.getCurrentParticipants()) {
                return R.fail("最大参与人数不能小于当前参与人数");
            }

            // 更新活动
            Activity activity = new Activity();
            BeanUtils.copyProperties(req, activity);
            int result = activityMapper.updateById(activity);
            
            if (result > 0) {
                return R.ok();
            } else {
                return R.fail("更新活动失败");
            }
        } catch (Exception e) {
            return R.fail("更新活动失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> deleteActivity(Long id) {
        try {
            Activity activity = activityMapper.selectById(id);
            if (activity == null) {
                return R.fail("活动不存在");
            }

            // 检查活动状态（实时判断）
            Integer currentStatus = ActivityStatusUtil.calculateStatus(activity);
            if (Objects.equals(currentStatus, ActivityStatusUtil.STATUS_ONGOING)) {
                return R.fail("进行中的活动不能删除");
            }

            int result = activityMapper.deleteById(id);
            if (result > 0) {
                return R.ok();
            } else {
                return R.fail("删除活动失败");
            }
        } catch (Exception e) {
            return R.fail("删除活动失败：" + e.getMessage());
        }
    }

    @Override
    public R<ActivityResp> getActivityById(Long id) {
        try {
            Activity activity = activityMapper.selectById(id);
            if (activity == null) {
                return R.fail("活动不存在");
            }

            ActivityResp resp = convertToActivityResp(activity);
            return R.ok(resp);
        } catch (Exception e) {
            return R.fail("查询活动失败：" + e.getMessage());
        }
    }

    @Override
    public R<PageRespDto<ActivityResp>> pageActivitiesAdmin(PageReqDto req) {
        try {
            int offset = (req.getPageNum() - 1) * req.getPageSize();
            
            // 查询活动列表
            List<Activity> activities = activityMapper.selectPage(offset, req.getPageSize());
            Integer total = activityMapper.selectCount();

            // 转换为响应对象
            List<ActivityResp> respList = activities.stream()
                    .map(this::convertToActivityResp)
                    .collect(Collectors.toList());

            PageRespDto<ActivityResp> pageResp = PageRespDto.of(req.getPageNum(), req.getPageSize(), total.longValue(), respList);
            return R.ok(pageResp);
        } catch (Exception e) {
            return R.fail("查询活动列表失败：" + e.getMessage());
        }
    }

    @Override
    public R<PageRespDto<ActivityResp>> pageAvailableActivities(PageReqDto req) {
        try {
            int offset = (req.getPageNum() - 1) * req.getPageSize();
            
            // 查询可参与的活动列表
            List<Activity> activities = activityMapper.selectAvailableActivities(offset, req.getPageSize());
            Integer total = activityMapper.selectAvailableActivitiesCount();

            // 转换为响应对象
            List<ActivityResp> respList = activities.stream()
                    .map(this::convertToActivityResp)
                    .collect(Collectors.toList());

            PageRespDto<ActivityResp> pageResp = PageRespDto.of(req.getPageNum(), req.getPageSize(), total.longValue(), respList);
            return R.ok(pageResp);
        } catch (Exception e) {
            return R.fail("查询可参与活动失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> publishActivity(Long id) {
        try {
            Activity activity = activityMapper.selectById(id);
            if (activity == null) {
                return R.fail("活动不存在");
            }

            // 如果活动已取消，不能启用
            if (activity.getStatus() != null && activity.getStatus() == ActivityStatusUtil.STATUS_CANCELLED) {
                return R.fail("已取消的活动不能启用");
            }

            // 根据当前时间重新计算活动状态并启用
            Integer newStatus = ActivityStatusUtil.calculateStatus(activity);
            int result = activityMapper.updateStatus(id, newStatus);
            
            if (result > 0) {
                return R.ok("活动已启用，当前状态：" + ActivityStatusUtil.getStatusName(newStatus));
            } else {
                return R.fail("启用活动失败");
            }
        } catch (Exception e) {
            return R.fail("启用活动失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> disableActivity(Long id) {
        try {
            Activity activity = activityMapper.selectById(id);
            if (activity == null) {
                return R.fail("活动不存在");
            }

            // 已取消的活动不能禁用
            if (activity.getStatus() != null && activity.getStatus() == ActivityStatusUtil.STATUS_CANCELLED) {
                return R.fail("已取消的活动不能禁用");
            }

            // 设置为已禁用状态
            int result = activityMapper.updateStatus(id, ActivityStatusUtil.STATUS_DISABLED);
            if (result > 0) {
                return R.ok("活动已禁用");
            } else {
                return R.fail("禁用活动失败");
            }
        } catch (Exception e) {
            return R.fail("禁用活动失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> cancelActivity(Long id) {
        try {
            Activity activity = activityMapper.selectById(id);
            if (activity == null) {
                return R.fail("活动不存在");
            }

            // 已取消的活动不能再次取消
            if (activity.getStatus() != null && activity.getStatus() == ActivityStatusUtil.STATUS_CANCELLED) {
                return R.fail("该活动已取消");
            }

            // 设置为已取消状态（永久性操作）
            int result = activityMapper.updateStatus(id, ActivityStatusUtil.STATUS_CANCELLED);
            if (result > 0) {
                return R.ok("活动已取消");
            } else {
                return R.fail("取消活动失败");
            }
        } catch (Exception e) {
            return R.fail("取消活动失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> participateActivity(Long activityId) {
        try {
            String currentUserId = loginHelper.getCurrentAppUserId();
            
            // 1. 检查活动是否存在（优先使用缓存）
            Activity activity;
            try {
                activity = activityCacheService.getActivity(activityId);
            } catch (Exception e) {
                // 缓存失败时降级到数据库查询
                log.warn("缓存获取活动失败，降级到数据库查询: {}", activityId, e);
                activity = activityMapper.selectById(activityId);
            }
            
            if (activity == null) {
                return R.fail("活动不存在");
            }

            // 2. 实时检查活动是否可以参与（直接基于时间判断，不依赖数据库状态）
            Date now = new Date();
            
            // 检查管理员设置的状态（已取消或已禁用）
            if (activity.getStatus() != null && 
                (activity.getStatus() == ActivityStatusUtil.STATUS_CANCELLED || 
                 activity.getStatus() == ActivityStatusUtil.STATUS_DISABLED)) {
                return R.fail("活动已" + ActivityStatusUtil.getStatusName(activity.getStatus()) + "，无法参与");
            }
            
            // 检查活动时间（实时判断）
            if (activity.getStartTime() == null || activity.getEndTime() == null) {
                return R.fail("活动时间设置不完整，无法参与");
            }
            
            if (now.before(activity.getStartTime())) {
                long secondsToStart = (activity.getStartTime().getTime() - now.getTime()) / 1000;
                return R.fail("活动尚未开始，还有 " + secondsToStart + " 秒开始");
            }
            
            if (now.after(activity.getEndTime())) {
                return R.fail("活动已结束，无法参与");
            }

            // 4. 检查是否已经参与过
            ActivityParticipant existingParticipant = activityParticipantMapper.selectByActivityAndUser(activityId, currentUserId);
            if (existingParticipant != null && existingParticipant.getStatus() == 0) {
                return R.fail("您已经参与过此活动");
            }

            // 5. 检查活动人数是否已满
            if (activity.getCurrentParticipants() >= activity.getMaxParticipants()) {
                return R.fail("活动人数已满");
            }

            // 6. 增加活动参与人数（使用乐观锁防止超员）
            int updateResult = activityMapper.increaseParticipantCount(activityId);
            if (updateResult == 0) {
                return R.fail("活动人数已满，参与失败");
            }

            // 7. 计算参与排名
            Integer currentCount = activityMapper.getCurrentParticipantCount(activityId);
            
            // 8. 创建参与记录
            ActivityParticipant participant = new ActivityParticipant();
            participant.setActivityId(activityId);
            participant.setUserId(currentUserId);
            participant.setParticipateTime(new Date());
            participant.setStatus(0); // 参与成功
            participant.setParticipantRank(currentCount);
            
            int insertResult = activityParticipantMapper.insert(participant);
            if (insertResult > 0) {
                return R.ok("参与成功！您是第" + currentCount + "位参与者");
            } else {
                // 如果插入失败，需要回滚参与人数
                activityMapper.decreaseParticipantCount(activityId);
                return R.fail("参与失败，请重试");
            }
            
        } catch (Exception e) {
            return R.fail("参与活动失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> cancelParticipation(Long activityId) {
        try {
            String currentUserId = loginHelper.getCurrentAppUserId();
            
            // 1. 检查活动是否存在
            Activity activity = activityMapper.selectById(activityId);
            if (activity == null) {
                return R.fail("活动不存在");
            }

            // 2. 检查是否已经参与
            ActivityParticipant participant = activityParticipantMapper.selectByActivityAndUser(activityId, currentUserId);
            if (participant == null || participant.getStatus() != 0) {
                return R.fail("您未参与此活动");
            }

            // 3. 检查是否可以取消（活动已开始后不能取消）
            Date now = new Date();
            if (now.after(activity.getStartTime())) {
                return R.fail("活动已开始，无法取消参与");
            }

            // 4. 更新参与状态为已取消
            int updateResult = activityParticipantMapper.updateStatus(participant.getId(), 1);
            if (updateResult == 0) {
                return R.fail("取消参与失败");
            }

            // 5. 减少活动参与人数
            activityMapper.decreaseParticipantCount(activityId);
            
            return R.ok("取消参与成功");
            
        } catch (Exception e) {
            return R.fail("取消参与失败：" + e.getMessage());
        }
    }

    @Override
    public R<PageRespDto<ActivityParticipantResp>> getActivityParticipants(Long activityId, PageReqDto req) {
        try {
            // 检查活动是否存在
            Activity activity = activityMapper.selectById(activityId);
            if (activity == null) {
                return R.fail("活动不存在");
            }

            int offset = (req.getPageNum() - 1) * req.getPageSize();
            
            // 查询参与者列表
            List<ActivityParticipant> participants = activityParticipantMapper.selectParticipantsByActivity(activityId, offset, req.getPageSize());
            Integer total = activityParticipantMapper.selectParticipantsCountByActivity(activityId);

            // 转换为响应对象
            List<ActivityParticipantResp> respList = participants.stream()
                    .map(participant -> convertToActivityParticipantResp(participant, activity))
                    .collect(Collectors.toList());

            PageRespDto<ActivityParticipantResp> pageResp = PageRespDto.of(req.getPageNum(), req.getPageSize(), total.longValue(), respList);
            return R.ok(pageResp);
        } catch (Exception e) {
            return R.fail("查询活动参与者失败：" + e.getMessage());
        }
    }

    @Override
    public R<PageRespDto<ActivityResp>> getUserParticipatedActivities(PageReqDto req) {
        try {
            String currentUserId = loginHelper.getCurrentAppUserId();
            int offset = (req.getPageNum() - 1) * req.getPageSize();
            
            // 查询用户参与的活动
            List<ActivityParticipant> participants = activityParticipantMapper.selectUserParticipatedActivities(currentUserId, offset, req.getPageSize());
            Integer total = activityParticipantMapper.selectUserParticipatedActivitiesCount(currentUserId);

            // 获取活动详情并转换为响应对象
            List<ActivityResp> respList = participants.stream()
                    .map(participant -> {
                        Activity activity = activityMapper.selectById(participant.getActivityId());
                        if (activity != null) {
                            ActivityResp resp = convertToActivityResp(activity);
                            resp.setParticipated(true);
                            return resp;
                        }
                        return null;
                    })
                    .filter(resp -> resp != null)
                    .collect(Collectors.toList());

            PageRespDto<ActivityResp> pageResp = PageRespDto.of(req.getPageNum(), req.getPageSize(), total.longValue(), respList);
            return R.ok(pageResp);
        } catch (Exception e) {
            return R.fail("查询用户参与活动失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> autoGrantPointsAfterActivity(Long activityId) {
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

            // 检查积分是否已经发放过（防重复发放机制）
            if (activity.getPointsGranted() != null && activity.getPointsGranted() == 1) {
                return R.fail("该活动积分已发放，请勿重复发放");
            }

            // 检查活动是否有积分奖励
            if (activity.getPointsAmount() == null || activity.getPointsAmount() <= 0) {
                return R.fail("该活动没有设置积分奖励");
            }

            // 获取成功参与的用户列表
            List<ActivityParticipant> successfulParticipants = activityParticipantMapper.selectSuccessfulParticipants(activityId);
            if (successfulParticipants.isEmpty()) {
                return R.fail("没有成功参与的用户");
            }

            // 生成批次号
            String batchNo = "ACTIVITY_" + activityId + "_" + System.currentTimeMillis();
            int successCount = 0;
            int failCount = 0;

            // 为每个参与者发放积分
            for (ActivityParticipant participant : successfulParticipants) {
                try {
                    // 检查是否已经发放过积分（双重保险）
                    PointsRecord existingRecord = pointsRecordMapper.selectByUserAndActivity(participant.getUserId(), activityId);
                    if (existingRecord != null) {
                        continue; // 跳过已发放的用户
                    }

                    // 创建积分发放记录
                    PointsRecord pointsRecord = new PointsRecord();
                    pointsRecord.setUserId(participant.getUserId());
                    pointsRecord.setActivityId(activityId);
                    pointsRecord.setPointsTypeId(activity.getPointsTypeId());
                    pointsRecord.setPointsAmount(activity.getPointsAmount());
                    pointsRecord.setOperationType(1); // 获得积分
                    pointsRecord.setStatus(0); // 待发放
                    pointsRecord.setBatchNo(batchNo);

                    // 插入积分记录
                    int insertResult = pointsRecordMapper.insert(pointsRecord);
                    if (insertResult > 0) {
                        // 为用户增加积分
                        boolean addPointsSuccess = addPointsToUser(participant.getUserId(), activity.getPointsTypeId(), activity.getPointsAmount());
                        
                        if (addPointsSuccess) {
                            // 更新积分发放状态为成功
                            pointsRecordMapper.updateGrantStatus(pointsRecord.getId(), 1, new Date(), null);
                            successCount++;
                        } else {
                            // 更新积分发放状态为失败
                            pointsRecordMapper.updateGrantStatus(pointsRecord.getId(), 2, null, "用户积分余额更新失败");
                            failCount++;
                        }
                    } else {
                        failCount++;
                    }
                } catch (Exception e) {
                    failCount++;
                }
            }

            // 标记活动积分已发放（无论成功失败，都标记为已处理）
            activityMapper.updatePointsGranted(activityId, 1);

            String message = "积分发放完成！成功发放: " + successCount + "人，失败: " + failCount + "人";
            return successCount > 0 ? R.ok(message) : R.fail("积分发放失败: " + message);
        } catch (Exception e) {
            return R.fail("自动发放积分失败：" + e.getMessage());
        }
    }

    /**
     * 转换Activity实体为ActivityResp
     */
    private ActivityResp convertToActivityResp(Activity activity) {
        ActivityResp resp = new ActivityResp();
        BeanUtils.copyProperties(activity, resp);

        // 设置积分类型名称
        if (activity.getPointsTypeId() != null) {
            PointsType pointsType = pointsTypeMapper.selectById(activity.getPointsTypeId());
            if (pointsType != null) {
                resp.setPointsTypeName(pointsType.getTypeName());
            }
        }

        // 设置活动类型名称
        resp.setActivityTypeName(getActivityTypeName(activity.getActivityType()));

        // 设置状态名称（使用实时计算的状态）
        Integer realTimeStatus = ActivityStatusUtil.calculateStatus(activity);
        resp.setStatusName(ActivityStatusUtil.getStatusName(realTimeStatus));

        // 设置是否已参与（需要查询参与记录表）
        String currentUserId = null;
        try {
            currentUserId = loginHelper.getCurrentAppUserId();
        } catch (Exception e) {
            // 未登录用户，设置为false
        }
        
        if (currentUserId != null) {
            ActivityParticipant participant = activityParticipantMapper.selectByActivityAndUser(activity.getId(), currentUserId);
            resp.setParticipated(participant != null && participant.getStatus() == 0);
                 } else {
             resp.setParticipated(false);
         }

        return resp;
    }

    /**
     * 获取活动类型名称
     */
    private String getActivityTypeName(Integer activityType) {
        if (activityType == null) return "";
        switch (activityType) {
            case 1: return "抢夺型";
            case 2: return "签到型";
            case 3: return "任务型";
            default: return "未知";
        }
    }

    /**
     * 获取活动状态名称
     */
    private String getActivityStatusName(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 0: return "待开始";
            case 1: return "进行中";
            case 2: return "已结束";
            case 3: return "已取消";
            default:         return "未知";
        }
    }

    /**
     * 转换ActivityParticipant实体为ActivityParticipantResp
     */
    private ActivityParticipantResp convertToActivityParticipantResp(ActivityParticipant participant, Activity activity) {
        ActivityParticipantResp resp = new ActivityParticipantResp();
        BeanUtils.copyProperties(participant, resp);

        // 设置活动信息
        if (activity != null) {
            resp.setActivityTitle(activity.getTitle());
        }

        // 设置状态名称
        resp.setStatusName(getParticipantStatusName(participant.getStatus()));

        // 设置用户名称
        resp.setUserName(userNameService.getUserNameById(participant.getUserId()));

        return resp;
    }

    /**
     * 获取参与状态名称
     */
    private String getParticipantStatusName(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 0: return "参与成功";
            case 1: return "已取消";
            case 2: return "违规取消";
            default: return "未知";
        }
    }

    /**
     * 为用户增加积分
     */
    private boolean addPointsToUser(String userId, Long pointsTypeId, Integer amount) {
        try {
            // 查找用户积分记录
            UserPoints userPoints = userPointsMapper.selectByUserAndPointsType(userId, pointsTypeId);
            
            if (userPoints == null) {
                // 如果不存在，创建新记录
                userPoints = new UserPoints();
                userPoints.setUserId(userId);
                userPoints.setPointsTypeId(pointsTypeId);
                userPoints.setCurrentBalance(amount);
                userPoints.setTotalEarned(amount);
                userPoints.setTotalSpent(0);
                userPoints.setFrozenPoints(0);
                
                return userPointsMapper.insert(userPoints) > 0;
            } else {
                // 更新现有记录
                return userPointsMapper.addPoints(userId, pointsTypeId, amount) > 0;
            }
        } catch (Exception e) {
            return false;
        }
    }
} 