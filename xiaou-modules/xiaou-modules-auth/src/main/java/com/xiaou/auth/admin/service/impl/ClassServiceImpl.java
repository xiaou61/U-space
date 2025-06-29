package com.xiaou.auth.admin.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.auth.admin.domain.entity.ClassEntity;
import com.xiaou.auth.admin.domain.req.ClassReq;
import com.xiaou.auth.admin.domain.resp.ClassResp;
import com.xiaou.auth.admin.mapper.ClassMapper;
import com.xiaou.auth.admin.service.ClassService;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.common.utils.QueryWrapperUtil;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClassServiceImpl extends ServiceImpl<ClassMapper, ClassEntity>
    implements ClassService {


    @Override
    public R<ClassResp> add(ClassReq req) {
        ClassEntity clazz = MapstructUtils.convert(req, ClassEntity.class);
        baseMapper.insert(clazz);
        return R.ok("添加成功");
    }

    @Override
    public R<String> delete(String id) {
        //todo 需要判断当前班级下有没有学生
        baseMapper.deleteById(id);
        return R.ok("删除成功");
    }

    @Override
    public R<ClassResp> updateClass(ClassReq req, String id) {
        //根据id 查询班级
        ClassEntity clazz = baseMapper.selectById(id);
        if (clazz == null) {
            return R.fail("班级不存在");
        }
        ClassEntity NewClassEntity = MapstructUtils.convert(req, clazz);
        baseMapper.updateById(NewClassEntity);
        return R.ok(MapstructUtils.convert(NewClassEntity, ClassResp.class));
    }

    @Override
    public R<PageRespDto<ClassResp>> pageClass(PageReqDto req) {
        IPage<ClassEntity> page = new Page<>(req.getPageNum(), req.getPageSize());
        QueryWrapper<ClassEntity> queryWrapper = new QueryWrapper<>();
        IPage<ClassEntity> classEntityIPage = baseMapper.selectPage(page, queryWrapper);

        return R.ok(PageRespDto.of(classEntityIPage.getCurrent(),
                classEntityIPage.getSize(),
                classEntityIPage.getTotal(),
                MapstructUtils.convert(classEntityIPage.getRecords(), ClassResp.class)));
    }
}




