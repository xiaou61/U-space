package com.xiaou.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.campus.domain.bo.CampusGuideBO;
import com.xiaou.campus.domain.entity.CampusGuide;
import com.xiaou.campus.domain.vo.CampusGuideVO;
import com.xiaou.campus.mapper.CampusGuideMapper;
import com.xiaou.campus.service.CampusGuideService;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CampusGuideServiceImpl extends ServiceImpl<CampusGuideMapper, CampusGuide> implements CampusGuideService {
    @Override
    public R<CampusGuideVO> addGuide(CampusGuideBO campusGuideBO) {
        CampusGuide entity = campusGuideBO.toEntity();
        baseMapper.insert(entity);
        return R.ok(CampusGuideVO.fromEntity(entity));
    }

    @Override
    public R<CampusGuideVO> updateGuide(Long id, CampusGuideBO campusGuideBO) {
        CampusGuide existing = baseMapper.selectById(id);
        if (existing == null || existing.getIsDeleted() == 1) {
            return R.fail("记录不存在或已被删除");
        }

        // 将BO转换成Entity，但保留原有ID和创建时间，更新时间要更新
        CampusGuide updated = campusGuideBO.toEntity();
        updated.setId(id);
        updated.setCreateTime(existing.getCreateTime());
        updated.setUpdateTime(LocalDateTime.now());
        updated.setIsDeleted(existing.getIsDeleted());

        int rows = baseMapper.updateById(updated);
        if (rows == 0) {
            return R.fail("更新失败");
        }
        return R.ok(CampusGuideVO.fromEntity(updated));
    }


    @Override
    public R<String> deleteGuide(Long id) {
        // 1. 查询记录是否存在
        CampusGuide existing = baseMapper.selectById(id);
        if (existing == null || existing.getIsDeleted() == 1) {
            return R.fail("记录不存在或已被删除");
        }
        // 2. 逻辑删除
        int rows = baseMapper.deleteById(id);
        if (rows == 0) {
            return R.fail("删除失败");
        }
        return R.ok("删除成功");
    }

    @Override
    public R<PageRespDto<CampusGuideVO>> allGuidePage(PageReqDto dto) {
        IPage<CampusGuide> page = new Page<>();
        page.setCurrent(dto.getPageNum());
        page.setSize(dto.getPageSize());

        QueryWrapper<CampusGuide> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(CampusGuide::getIsDeleted, 0); // 只查未删除

        IPage<CampusGuide> campusGuidePage = baseMapper.selectPage(page, queryWrapper);

        List<CampusGuideVO> voList = campusGuidePage.getRecords().stream()
                .map(CampusGuideVO::fromEntity)
                .toList();

        return R.ok(PageRespDto.of(dto.getPageNum(), dto.getPageSize(), campusGuidePage.getTotal(), voList));
    }



}
