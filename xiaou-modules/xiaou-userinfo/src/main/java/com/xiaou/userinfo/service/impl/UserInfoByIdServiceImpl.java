package com.xiaou.userinfo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiaou.common.domain.R;
import com.xiaou.userinfo.domain.entity.ClassEntity;
import com.xiaou.userinfo.domain.entity.Major;
import com.xiaou.userinfo.domain.entity.StudentInfoLink;
import com.xiaou.userinfo.domain.vo.UClassVO;
import com.xiaou.userinfo.mapper.ClassMapper;
import com.xiaou.userinfo.mapper.MajorMapper;
import com.xiaou.userinfo.mapper.StudentInfoLinkMapper;
import com.xiaou.userinfo.service.UserInfoByIdService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserInfoByIdServiceImpl implements UserInfoByIdService {

    @Resource
    private MajorMapper majorMapper;

    @Resource
    private ClassMapper classMapper;

    @Resource
    private StudentInfoLinkMapper basemapper;

    @Override
    public R<List<Major>> getMajor(Long id) {
        QueryWrapper<Major> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("college_id", id);

        return R.ok(majorMapper.selectList(queryWrapper));
    }

    @Override
    public R<List<ClassEntity>> getClazz(Long id) {
        QueryWrapper<ClassEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("major_id", id);
        return R.ok(classMapper.selectList(queryWrapper));
    }

}
