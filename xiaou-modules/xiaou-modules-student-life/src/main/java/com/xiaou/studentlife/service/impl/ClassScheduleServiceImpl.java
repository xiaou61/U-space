package com.xiaou.studentlife.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.common.utils.QueryWrapperUtil;
import com.xiaou.studentlife.domain.bo.ClassScheduleBo;
import com.xiaou.studentlife.domain.entity.ClassSchedule;
import com.xiaou.studentlife.domain.vo.ClassScheduleVo;
import com.xiaou.studentlife.mapper.ClassScheduleMapper;
import com.xiaou.studentlife.service.ClassScheduleService;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ClassScheduleServiceImpl extends ServiceImpl<ClassScheduleMapper, ClassSchedule>
        implements ClassScheduleService {

    @Override
    public R<ClassScheduleVo> addClassSchedule(ClassScheduleBo scheduleBo) {
        ClassSchedule classSchedule = MapstructUtils.convert(scheduleBo, ClassSchedule.class);
        baseMapper.insert(classSchedule);
        ClassScheduleVo classScheduleVo = MapstructUtils.convert(classSchedule, ClassScheduleVo.class);
        if (classScheduleVo != null) {
            return R.ok(classScheduleVo);
        }
        return null;
    }

    @Override
    public R<String> deleteClassSchedule(Long id) {
        baseMapper.deleteById(id);
        return R.ok("删除成功");
    }

    @Override
    public R<ClassScheduleVo> updateClassSchedule(Long id, ClassScheduleBo scheduleBo) {
        ClassSchedule classSchedule = MapstructUtils.convert(scheduleBo, ClassSchedule.class);
        classSchedule.setId(id);
        baseMapper.updateById(classSchedule);
        ClassScheduleVo vo = MapstructUtils.convert(classSchedule, ClassScheduleVo.class);
        return R.ok(vo);
    }

    @Override
    public R<List<ClassScheduleVo>> listClassSchedule(Long userId) {
        //根据userId查询classid
        Long classId = baseMapper.selectClassIdByUserId(userId);
        //根据classid查询
        QueryWrapper<ClassSchedule> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("class_id", classId);
        List<ClassSchedule> classSchedules = baseMapper.selectList(queryWrapper);
        if (classSchedules != null) {
            List<ClassScheduleVo> classScheduleVos = MapstructUtils.convert(classSchedules, ClassScheduleVo.class);
            return R.ok(classScheduleVos);
        }
        return null;
    }

    @Override
    public R<PageRespDto<ClassScheduleVo>> listClassSchedulePage(PageReqDto dto) {
        IPage<ClassSchedule> page = new Page<>();
        page.setCurrent(dto.getPageNum());
        page.setSize(dto.getPageSize());
        // 添加排序字段（以 create_time 字段为例）
        QueryWrapper<ClassSchedule> queryWrapper = new QueryWrapper<>();
        QueryWrapperUtil.applySorting(queryWrapper, dto, List.of(dto.getSortField()));
        IPage<ClassSchedule> classScheduleIPage = baseMapper.selectPage(page, queryWrapper);
        List<ClassScheduleVo> classScheduleVos = MapstructUtils.convert(classScheduleIPage.getRecords(), ClassScheduleVo.class);

        return R.ok(PageRespDto.of(dto.getPageNum(), dto.getPageSize(), classScheduleIPage.getTotal(), classScheduleVos));
    }


}




