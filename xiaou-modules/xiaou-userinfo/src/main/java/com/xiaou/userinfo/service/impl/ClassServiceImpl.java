package com.xiaou.userinfo.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.userinfo.domain.bo.UClassBO;
import com.xiaou.userinfo.domain.entity.ClassEntity;
import com.xiaou.userinfo.domain.entity.Major;
import com.xiaou.userinfo.domain.vo.UClassVO;
import com.xiaou.userinfo.mapper.ClassMapper;
import com.xiaou.userinfo.mapper.MajorMapper;
import com.xiaou.userinfo.service.ClassService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.xiaou.utils.LoginHelper.getCurrentUsername;

@Service
public class ClassServiceImpl extends ServiceImpl<ClassMapper, ClassEntity> implements ClassService {

    @Resource
    private ClassMapper classMapper;

    @Resource
    private MajorMapper majorMapper;

    @Override
    public R<UClassVO> addClass(UClassBO classBO) {
        ClassEntity clazz = new ClassEntity();
        BeanUtils.copyProperties(classBO, clazz);
        clazz.setCreatedBy(getCurrentUsername());
        clazz.setCreatedTime(LocalDateTime.now());
        classMapper.insert(clazz);

        String majorName = getMajorNameById(clazz.getMajorId());
        return R.ok(UClassVO.fromEntity(clazz, majorName));
    }

    @Override
    public R<UClassVO> updateClass(Long id, UClassBO classBO) {
        ClassEntity clazz = classMapper.selectById(id);
        if (clazz == null) {
            return R.fail("班级不存在，无法更新");
        }
        BeanUtils.copyProperties(classBO, clazz);
        clazz.setClassId(id);
        clazz.setUpdatedBy(getCurrentUsername());
        clazz.setUpdatedTime(LocalDateTime.now());
        classMapper.updateById(clazz);

        String majorName = getMajorNameById(clazz.getMajorId());
        return R.ok(UClassVO.fromEntity(clazz, majorName));
    }

    @Override
    public R<Void> deleteClass(Long id) {
        int rows = classMapper.deleteById(id);
        if (rows > 0) {
            return R.ok();
        } else {
            return R.fail("班级不存在或已被删除");
        }
    }

    @Override
    public R<PageRespDto<UClassVO>> allClassPage(PageReqDto dto) {
        IPage<ClassEntity> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        IPage<ClassEntity> classPage = classMapper.selectPage(page, new QueryWrapper<>());

        List<UClassVO> voList = classPage.getRecords().stream().map(clazz -> {
            String majorName = getMajorNameById(clazz.getMajorId());
            return UClassVO.fromEntity(clazz, majorName);
        }).toList();

        return R.ok(PageRespDto.of(
                dto.getPageNum(),
                dto.getPageSize(),
                classPage.getTotal(),
                voList
        ));
    }

    @Override
    public R<UClassVO> getClassById(Long id) {
        ClassEntity classEntity = classMapper.selectById(id);
        if (classEntity == null) {
            return R.fail("班级不存在");
        }
        return R.ok(UClassVO.fromEntity(classEntity, getMajorNameById(id)));
    }


    private String getMajorNameById(Long majorId) {
        if (majorId == null) return null;
        Major major = majorMapper.selectById(majorId);
        return major != null ? major.getName() : null;
    }
}
