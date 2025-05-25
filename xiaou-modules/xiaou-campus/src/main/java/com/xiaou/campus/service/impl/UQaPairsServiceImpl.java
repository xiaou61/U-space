package com.xiaou.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.campus.domain.entity.QaPairs;
import com.xiaou.campus.mapper.QaPairsMapper;
import com.xiaou.campus.service.QaPairsService;

import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import org.springframework.stereotype.Service;

@Service
public class UQaPairsServiceImpl extends ServiceImpl<QaPairsMapper, QaPairs>
        implements QaPairsService {

    @Override
    public R<QaPairs> addQA(QaPairs qaPairs) {
        baseMapper.insert(qaPairs);
        return R.ok(qaPairs);
    }

    @Override
    public R<QaPairs> deleteQA(Long id) {
        baseMapper.deleteById(id);
        return R.ok();
    }

    @Override
    public R<QaPairs> updateQA(QaPairs qaPairs) {
        baseMapper.updateById(qaPairs);
        return R.ok(qaPairs);
    }

    @Override
    public R<PageRespDto<QaPairs>> allQaPage(PageReqDto dto) {
        IPage<QaPairs> page = new Page<>();
        page.setCurrent(dto.getPageNum());
        page.setSize(dto.getPageSize());
        IPage<QaPairs> collegeIPage = baseMapper.selectPage(page, new QueryWrapper<>());
        return R.ok(PageRespDto.of(dto.getPageNum(), dto.getPageSize(), collegeIPage.getTotal(), collegeIPage.getRecords()));
    }


}




