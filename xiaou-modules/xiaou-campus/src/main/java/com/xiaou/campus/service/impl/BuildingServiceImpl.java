package com.xiaou.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.campus.domain.bo.BuildingInfoBO;
import com.xiaou.campus.domain.entity.BuildingInfo;
import com.xiaou.campus.domain.vo.BuildingInfoVO;
import com.xiaou.campus.mapper.BuildingMapper;
import com.xiaou.campus.mapper.PlaceCategoryMapper;
import com.xiaou.campus.service.BuildingService;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.common.utils.QueryWrapperUtil;
import com.xiaou.utils.LoginHelper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BuildingServiceImpl extends ServiceImpl<BuildingMapper, BuildingInfo> implements BuildingService {

    @Resource
    private PlaceCategoryMapper placeCategoryMapper;

    @Override
    public R<BuildingInfoVO> addBuild(BuildingInfoBO buildingInfoBO) {
        BuildingInfo entity = buildingInfoBO.toEntity();
        entity.setCreateBy(LoginHelper.getCurrentUsername());
        baseMapper.insert(entity);
        String CategoryName = placeCategoryMapper.selectById(entity.getCategoryId()).getName();
        return R.ok(BuildingInfoVO.fromEntity(entity, CategoryName));
    }

    @Override
    public R<BuildingInfoVO> updateBuild(Long id, BuildingInfoBO buildingInfoBO) {
        BuildingInfo existing = baseMapper.selectById(id);
        if (existing == null) {
            return R.fail("更新失败，建筑信息不存在");
        }

        BuildingInfo updateEntity = buildingInfoBO.toEntity();
        updateEntity.setId(id);
        updateEntity.setCategoryId(existing.getCategoryId()); // 保持分类不变
        updateEntity.setUpdateBy(LoginHelper.getCurrentUsername());

        int rows = baseMapper.updateById(updateEntity);
        if (rows <= 0) {
            return R.fail("更新失败");
        }

        String categoryName = placeCategoryMapper.selectById(updateEntity.getCategoryId()).getName();
        return R.ok(BuildingInfoVO.fromEntity(updateEntity, categoryName));
    }


    @Override
    public R<String> deleteBuild(Long id) {
        int rows = baseMapper.deleteById(id);
        if (rows > 0) {
            return R.ok("删除成功");
        } else {
            return R.fail("删除失败，未找到该建筑信息");
        }
    }

    @Override
    public R<PageRespDto<BuildingInfoVO>> allGuidePage(PageReqDto dto) {
        IPage<BuildingInfo> page = new Page<>();
        page.setCurrent(dto.getPageNum());
        page.setSize(dto.getPageSize());
        // 添加排序字段（以 create_time 字段为例）
        QueryWrapper<BuildingInfo> queryWrapper = new QueryWrapper<>();
        QueryWrapperUtil.applySorting(queryWrapper, dto, List.of(dto.getSortField()));
        IPage<BuildingInfo> buildingPage = baseMapper.selectPage(page, queryWrapper);
        // 转换实体到VO
        List<BuildingInfoVO> voList = buildingPage.getRecords().stream().map(entity -> {
            // 先查分类名
            String categoryName = null;
            if (entity.getCategoryId() != null) {
                var category = placeCategoryMapper.selectById(entity.getCategoryId());
                if (category != null) {
                    categoryName = category.getName();
                }
            }
            return BuildingInfoVO.fromEntity(entity, categoryName);
        }).toList();
        return R.ok(PageRespDto.of(dto.getPageNum(), dto.getPageSize(), buildingPage.getTotal(), voList));

    }
}
