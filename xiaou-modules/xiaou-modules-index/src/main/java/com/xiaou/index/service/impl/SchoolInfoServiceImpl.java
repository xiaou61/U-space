package com.xiaou.index.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.common.constant.GlobalConstants;
import com.xiaou.common.domain.R;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.index.domain.entity.SchoolInfo;
import com.xiaou.index.domain.req.SchoolInfoReq;
import com.xiaou.index.domain.resp.SchoolInfoResp;
import com.xiaou.index.mapper.SchoolInfoMapper;
import com.xiaou.index.service.SchoolInfoService;
import org.springframework.stereotype.Service;

@Service
public class SchoolInfoServiceImpl extends ServiceImpl<SchoolInfoMapper, SchoolInfo>
    implements SchoolInfoService {

    @Override
    public R<String> updateSchoolInfo(SchoolInfoReq req) {
        //查找该学校
        SchoolInfo schoolInfo = baseMapper.selectById(GlobalConstants.ONE);
        SchoolInfo convert = MapstructUtils.convert(req, schoolInfo);
        baseMapper.updateById(convert);
        return R.ok("更新成功");
    }

    @Override
    public R<SchoolInfoResp> info() {
        SchoolInfo schoolInfo = baseMapper.selectById(GlobalConstants.ONE);
        SchoolInfoResp convert = MapstructUtils.convert(schoolInfo, SchoolInfoResp.class);
        return R.ok(convert);
    }
}




