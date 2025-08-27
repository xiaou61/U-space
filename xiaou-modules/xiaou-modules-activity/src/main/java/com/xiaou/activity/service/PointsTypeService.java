package com.xiaou.activity.service;

import com.xiaou.activity.domain.req.PointsTypeCreateReq;
import com.xiaou.activity.domain.resp.PointsTypeResp;
import com.xiaou.common.domain.R;

import java.util.List;

/**
 * 积分类型Service接口
 */
public interface PointsTypeService {

    /**
     * 创建积分类型
     * @param req 创建请求
     * @return 操作结果
     */
    R<Long> createPointsType(PointsTypeCreateReq req);

    /**
     * 更新积分类型状态
     * @param id 积分类型ID
     * @param status 状态(0:禁用 1:启用)
     * @return 操作结果
     */
    R<Void> updatePointsTypeStatus(Long id, Integer status);

    /**
     * 删除积分类型
     * @param id 积分类型ID
     * @return 操作结果
     */
    R<Void> deletePointsType(Long id);

    /**
     * 获取积分类型列表
     * @return 积分类型列表
     */
    R<List<PointsTypeResp>> getPointsTypeList();

    /**
     * 获取启用的积分类型列表
     * @return 启用的积分类型列表
     */
    R<List<PointsTypeResp>> getActivePointsTypeList();
} 