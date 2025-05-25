package com.xiaou.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.campus.domain.bo.PlaceCategoryBO;
import com.xiaou.campus.domain.entity.BuildingInfo;
import com.xiaou.campus.domain.entity.CampusGuide;
import com.xiaou.campus.domain.entity.PlaceCategory;
import com.xiaou.campus.domain.vo.PlaceCategoryVO;
import com.xiaou.campus.mapper.BuildingMapper;
import com.xiaou.campus.mapper.PlaceCategoryMapper;
import com.xiaou.campus.service.PlaceCategoryService;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.utils.LoginHelper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PlaceCategoryServiceImpl extends ServiceImpl<PlaceCategoryMapper, PlaceCategory> implements PlaceCategoryService {
    @Resource
    private PlaceCategoryMapper placeCategoryMapper;

    @Resource
    private BuildingMapper buildingMapper;

    @Override
    public R<PlaceCategoryVO> addCategory(PlaceCategoryBO bo) {
        PlaceCategory placeCategory = MapstructUtils.convert(bo, PlaceCategory.class);
        placeCategory.setCreateTime(LocalDateTime.now());
        placeCategory.setCreateBy(LoginHelper.getCurrentUsername());
        placeCategoryMapper.insert(placeCategory);
        PlaceCategoryVO placeCategoryVO = MapstructUtils.convert(placeCategory, PlaceCategoryVO.class);
        return R.ok(placeCategoryVO);
    }

    @Override
    public R<Boolean> deleteCategory(Long id) {
        PlaceCategory existing = placeCategoryMapper.selectById(id);
        if (existing == null) {
            return R.fail("分类不存在");
        }

        // 判断该分类下是否有建筑信息
        QueryWrapper<BuildingInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category_id", id);
        Long count = buildingMapper.selectCount(queryWrapper);
        if (count > 0) {
            return R.fail("该分类下存在地点信息，不能删除");
        }

        int rows = placeCategoryMapper.deleteById(id);
        return rows > 0 ? R.ok(true) : R.fail("删除失败");
    }


    @Override
    public R<PlaceCategoryVO> updateCategory(Long id, PlaceCategoryBO bo) {
        PlaceCategory existing = placeCategoryMapper.selectById(id);
        if (existing == null) {
            return R.fail("分类不存在");
        }

        // 更新字段
        existing.setName(bo.getName());
        existing.setUpdateTime(LocalDateTime.now());
        existing.setUpdateBy(LoginHelper.getCurrentUsername());

        int rows = placeCategoryMapper.updateById(existing);
        if (rows == 0) {
            return R.fail("更新失败");
        }
        PlaceCategoryVO vo = MapstructUtils.convert(existing, PlaceCategoryVO.class);
        return R.ok(vo);
    }

    @Override
    public R<PageRespDto<PlaceCategoryVO>> list(PageReqDto dto) {
        IPage<PlaceCategory> page = placeCategoryMapper.selectPage(
                new Page<>(dto.getPageNum(), dto.getPageSize()),
                new QueryWrapper<>()
        );

        List<PlaceCategoryVO> voList = MapstructUtils.convert(page.getRecords(), PlaceCategoryVO.class);

        return R.ok(PageRespDto.of(dto.getPageNum(), dto.getPageSize(), page.getTotal(), voList));
    }


}
