package com.xiaou.activity.service;

import com.xiaou.activity.domain.req.PointsGrantReq;
import com.xiaou.activity.domain.resp.PointsRecordResp;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;

/**
 * 积分记录Service接口
 */
public interface PointsRecordService {

    /**
     * 手动发放积分
     * @param req 积分发放请求
     * @return 操作结果
     */
    R<Void> grantPointsManually(PointsGrantReq req);

    /**
     * 批量发放积分（活动结束后）
     * @param activityId 活动ID
     * @return 操作结果
     */
    R<Void> batchGrantPointsForActivity(Long activityId);

    /**
     * 撤销积分发放
     * @param recordId 积分记录ID
     * @return 操作结果
     */
    R<Void> revokePointsRecord(Long recordId);

    /**
     * 分页查询积分发放记录
     * @param req 分页请求
     * @return 积分记录列表
     */
    R<PageRespDto<PointsRecordResp>> pagePointsRecords(PageReqDto req);

    /**
     * 查询用户积分记录
     * @param userId 用户ID
     * @param req 分页请求
     * @return 用户积分记录列表
     */
    R<PageRespDto<PointsRecordResp>> pageUserPointsRecords(String userId, PageReqDto req);

    /**
     * 查询当前用户积分记录
     * @param req 分页请求
     * @return 当前用户积分记录列表
     */
    R<PageRespDto<PointsRecordResp>> pageCurrentUserPointsRecords(PageReqDto req);
} 