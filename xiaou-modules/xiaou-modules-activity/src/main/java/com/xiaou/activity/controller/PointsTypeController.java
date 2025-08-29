package com.xiaou.activity.controller;

import com.xiaou.activity.domain.req.PointsTypeCreateReq;
import com.xiaou.activity.domain.req.PointsTypeUpdateReq;
import com.xiaou.activity.domain.resp.PointsTypeResp;
import com.xiaou.activity.service.PointsTypeService;
import com.xiaou.common.domain.R;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 积分类型管理Controller
 */
@RestController
@RequestMapping("/admin/points/types")
@Validated
public class PointsTypeController {

    @Resource
    private PointsTypeService pointsTypeService;

    /**
     * 获取积分类型列表
     */
    @PostMapping("/list")
    public R<List<PointsTypeResp>> getPointsTypeList() {
        return pointsTypeService.getPointsTypeList();
    }

    /**
     * 创建积分类型
     */
    @PostMapping
    public R<Long> createPointsType(@RequestBody @Validated PointsTypeCreateReq req) {
        return pointsTypeService.createPointsType(req);
    }

    /**
     * 更新积分类型
     */
    @PutMapping("/{id}")
    public R<Void> updatePointsType(@PathVariable Long id, @RequestBody @Validated PointsTypeUpdateReq req) {
        return pointsTypeService.updatePointsType(id, req);
    }

    /**
     * 删除积分类型
     */
    @DeleteMapping("/{id}")
    public R<Void> deletePointsType(@PathVariable Long id) {
        return pointsTypeService.deletePointsType(id);
    }

    /**
     * 启用/禁用积分类型
     */
    @PutMapping("/{id}/status")
    public R<Void> togglePointsTypeStatus(@PathVariable Long id, @RequestBody PointsTypeStatusReq req) {
        return pointsTypeService.updatePointsTypeStatus(id, req.getIsActive());
    }

    /**
     * 积分类型状态更新请求
     */
    public static class PointsTypeStatusReq {
        private Integer isActive;

        public Integer getIsActive() {
            return isActive;
        }

        public void setIsActive(Integer isActive) {
            this.isActive = isActive;
        }
    }
} 