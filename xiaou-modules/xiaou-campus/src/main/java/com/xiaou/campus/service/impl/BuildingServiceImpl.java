package com.xiaou.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.campus.domain.bo.BuildingInfoBO;
import com.xiaou.campus.domain.entity.BuildingInfo;
import com.xiaou.campus.domain.entity.PlaceCategory;
import com.xiaou.campus.domain.vo.BuildingInfoVO;
import com.xiaou.campus.domain.vo.CategoryWithBuildingListVO;
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
import java.util.stream.Collectors;

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
    public R<PageRespDto<CategoryWithBuildingListVO>> allGuidePage(PageReqDto dto) {
        // 1. 分页查分类
        IPage<PlaceCategory> categoryPage = placeCategoryMapper.selectPage(
                new Page<>(dto.getPageNum(), dto.getPageSize()),
                new QueryWrapper<PlaceCategory>()
        );

        // 2. 对每个分类查对应建筑物列表
        List<CategoryWithBuildingListVO> voList = categoryPage.getRecords().stream().map(category -> {
            // 查该分类下所有建筑物（不分页）
            List<BuildingInfo> buildingList = baseMapper.selectList(
                    new QueryWrapper<BuildingInfo>().eq("category_id", category.getId())
            );

            // 转换建筑物实体为VO
            List<BuildingInfoVO> buildingVOList = buildingList.stream()
                    .map(b -> BuildingInfoVO.fromEntity(b, category.getName()))
                    .collect(Collectors.toList());

            // 返回带嵌套建筑物列表的分类VO
            return new CategoryWithBuildingListVO(category.getId(), category.getName(), buildingVOList);
        }).collect(Collectors.toList());

        // 3. 返回分页结果（分页的是分类）
        return R.ok(PageRespDto.of(dto.getPageNum(), dto.getPageSize(), categoryPage.getTotal(), voList));
    }

}
