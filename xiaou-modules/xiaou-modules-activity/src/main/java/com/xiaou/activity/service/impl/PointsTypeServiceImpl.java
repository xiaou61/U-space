package com.xiaou.activity.service.impl;

import com.xiaou.activity.domain.entity.PointsType;
import com.xiaou.activity.domain.req.PointsTypeCreateReq;
import com.xiaou.activity.domain.resp.PointsTypeResp;
import com.xiaou.activity.mapper.PointsRecordMapper;
import com.xiaou.activity.mapper.PointsTypeMapper;
import com.xiaou.activity.mapper.UserPointsMapper;
import com.xiaou.activity.service.PointsTypeService;
import com.xiaou.common.domain.R;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.stream.Collectors;

/**
 * 积分类型Service实现类
 */
@Service
public class PointsTypeServiceImpl implements PointsTypeService {

    @Resource
    private PointsTypeMapper pointsTypeMapper;

    @Resource
    private PointsRecordMapper pointsRecordMapper;

    @Resource
    private UserPointsMapper userPointsMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Long> createPointsType(PointsTypeCreateReq req) {
        try {
            // 创建积分类型实体
            PointsType pointsType = new PointsType();
            BeanUtils.copyProperties(req, pointsType);
            pointsType.setIsActive(1); // 默认启用

            // 插入数据库
            int result = pointsTypeMapper.insert(pointsType);
            if (result > 0) {
                return R.ok(pointsType.getId());
            } else {
                return R.fail("创建积分类型失败");
            }
        } catch (Exception e) {
            return R.fail("创建积分类型失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> updatePointsTypeStatus(Long id, Integer status) {
        try {
            // 检查积分类型是否存在
            PointsType pointsType = pointsTypeMapper.selectById(id);
            if (pointsType == null) {
                return R.fail("积分类型不存在");
            }

            // 更新状态
            int result = pointsTypeMapper.updateStatus(id, status);
            if (result > 0) {
                return R.ok();
            } else {
                return R.fail("更新积分类型状态失败");
            }
        } catch (Exception e) {
            return R.fail("更新积分类型状态失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R<Void> deletePointsType(Long id) {
        try {
            // 检查积分类型是否存在
            PointsType pointsType = pointsTypeMapper.selectById(id);
            if (pointsType == null) {
                return R.fail("积分类型不存在");
            }

            // 检查是否有相关的积分记录，有的话不允许删除
            List<com.xiaou.activity.domain.entity.PointsRecord> relatedRecords = pointsRecordMapper.selectByPointsTypeId(id);
            if (!relatedRecords.isEmpty()) {
                return R.fail("该积分类型存在相关积分记录，无法删除");
            }
            
            // 检查是否有用户积分余额记录
            List<com.xiaou.activity.domain.entity.UserPoints> userPointsRecords = userPointsMapper.selectByPointsTypeId(id);
            if (!userPointsRecords.isEmpty()) {
                return R.fail("该积分类型存在用户积分余额记录，无法删除");
            }

            // 删除积分类型
            int result = pointsTypeMapper.deleteById(id);
            if (result > 0) {
                return R.ok();
            } else {
                return R.fail("删除积分类型失败");
            }
        } catch (Exception e) {
            return R.fail("删除积分类型失败：" + e.getMessage());
        }
    }

    @Override
    public R<List<PointsTypeResp>> getPointsTypeList() {
        try {
            List<PointsType> pointsTypes = pointsTypeMapper.selectAll();
            List<PointsTypeResp> respList = pointsTypes.stream()
                    .map(this::convertToPointsTypeResp)
                    .collect(Collectors.toList());
            return R.ok(respList);
        } catch (Exception e) {
            return R.fail("查询积分类型列表失败：" + e.getMessage());
        }
    }

    @Override
    public R<List<PointsTypeResp>> getActivePointsTypeList() {
        try {
            List<PointsType> pointsTypes = pointsTypeMapper.selectActiveList();
            List<PointsTypeResp> respList = pointsTypes.stream()
                    .map(this::convertToPointsTypeResp)
                    .collect(Collectors.toList());
            return R.ok(respList);
        } catch (Exception e) {
            return R.fail("查询启用的积分类型列表失败：" + e.getMessage());
        }
    }

    /**
     * 转换PointsType实体为PointsTypeResp
     */
    private PointsTypeResp convertToPointsTypeResp(PointsType pointsType) {
        PointsTypeResp resp = new PointsTypeResp();
        BeanUtils.copyProperties(pointsType, resp);
        return resp;
    }
} 