package com.xiaou.activity.service;

import com.xiaou.activity.domain.req.ActivityCreateReq;
import com.xiaou.activity.domain.req.ActivityUpdateReq;
import com.xiaou.activity.domain.resp.ActivityResp;
import com.xiaou.activity.domain.resp.ActivityParticipantResp;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;

/**
 * 活动Service接口
 */
public interface ActivityService {

    /**
     * 创建活动
     * @param req 创建请求
     * @return 活动ID
     */
    R<Long> createActivity(ActivityCreateReq req);

    /**
     * 更新活动
     * @param req 更新请求
     * @return 操作结果
     */
    R<Void> updateActivity(ActivityUpdateReq req);

    /**
     * 删除活动
     * @param id 活动ID
     * @return 操作结果
     */
    R<Void> deleteActivity(Long id);

    /**
     * 获取活动详情
     * @param id 活动ID
     * @return 活动详情
     */
    R<ActivityResp> getActivityById(Long id);

    /**
     * 分页查询活动列表（管理员）
     * @param req 分页请求
     * @return 活动列表
     */
    R<PageRespDto<ActivityResp>> pageActivitiesAdmin(PageReqDto req);

    /**
     * 获取可参与的活动列表（用户端）
     * @param req 分页请求
     * @return 活动列表
     */
    R<PageRespDto<ActivityResp>> pageAvailableActivities(PageReqDto req);

    /**
     * 启用活动（重新计算状态）
     * @param id 活动ID
     * @return 操作结果
     */
    R<Void> publishActivity(Long id);

    /**
     * 禁用活动（暂停活动，可重新启用）
     * @param id 活动ID
     * @return 操作结果
     */
    R<Void> disableActivity(Long id);

    /**
     * 取消活动（永久取消，不可恢复）
     * @param id 活动ID
     * @return 操作结果
     */
    R<Void> cancelActivity(Long id);

    /**
     * 参与活动（用户抢夺）
     * @param activityId 活动ID
     * @return 操作结果
     */
    R<Void> participateActivity(Long activityId);

    /**
     * 取消参与活动
     * @param activityId 活动ID
     * @return 操作结果
     */
    R<Void> cancelParticipation(Long activityId);

    /**
     * 获取活动参与者列表
     * @param activityId 活动ID
     * @param req 分页请求
     * @return 参与者列表
     */
    R<PageRespDto<ActivityParticipantResp>> getActivityParticipants(Long activityId, PageReqDto req);

    /**
     * 查询用户参与的活动
     * @param req 分页请求
     * @return 用户参与的活动列表
     */
    R<PageRespDto<ActivityResp>> getUserParticipatedActivities(PageReqDto req);

    /**
     * 活动结束后自动发放积分
     * @param activityId 活动ID
     * @return 操作结果
     */
    R<Void> autoGrantPointsAfterActivity(Long activityId);
} 