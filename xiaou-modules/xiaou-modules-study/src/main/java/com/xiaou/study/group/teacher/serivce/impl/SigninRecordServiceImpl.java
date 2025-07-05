package com.xiaou.study.group.teacher.serivce.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.common.domain.R;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.study.group.teacher.domain.entity.Signin;
import com.xiaou.study.group.teacher.domain.entity.SigninRecord;
import com.xiaou.study.group.teacher.domain.req.SigninRecordReq;
import com.xiaou.study.group.teacher.domain.resp.SigninResp;
import com.xiaou.study.group.teacher.handler.SigninHandler;
import com.xiaou.study.group.teacher.mapper.SigninMapper;
import com.xiaou.study.group.teacher.mapper.SigninRecordMapper;
import com.xiaou.study.group.teacher.serivce.SigninRecordService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class SigninRecordServiceImpl extends ServiceImpl<SigninRecordMapper, SigninRecord>
    implements SigninRecordService {

    @Resource
    private SigninMapper signinMapper;

    @Resource
    private List<SigninHandler> signinHandlers;
    @Override
    public R<SigninRecord> add(SigninRecordReq req) {
        //首先判断是什么签到
        for (SigninHandler handler : signinHandlers) {
            if (handler.support(req.getType())) {
                handler.executeSignin(req);
                return R.ok("签到成功");
            }
        }
        return R.fail("不支持的签到类型");
    }

    @Override
    public R<List<SigninResp>> listMy(String groupId) {
        //根据groupId查询
        QueryWrapper<Signin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("groupId", groupId);
        List<Signin> signins = signinMapper.selectList(queryWrapper);
        if (signins != null && !signins.isEmpty()) {
            List<SigninResp> convert = MapstructUtils.convert(signins, SigninResp.class);
            return R.ok(convert);
        }
        return R.ok(Collections.emptyList());
    }
}




