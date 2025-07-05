package com.xiaou.study.group.teacher.handler;

import com.xiaou.common.exception.ServiceException;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.satoken.utils.LoginHelper;
import com.xiaou.study.group.teacher.domain.entity.Signin;
import com.xiaou.study.group.teacher.domain.req.SigninRecordReq;
import com.xiaou.study.group.teacher.domain.req.SigninReq;
import com.xiaou.study.group.teacher.mapper.SigninMapper;
import com.xiaou.study.group.teacher.serivce.SigninService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class LocationSigninHandler implements SigninHandler{

    @Resource
    private LoginHelper loginHelper;
    @Resource
    private SigninMapper signinMapper;
    @Override
    public boolean support(Integer type) {
        return type==2;
    }

    @Override
    public void handle(SigninReq req) {
        //位置签到需要判断位置是否在
        if (req.getLatitude()==null && req.getLongitude()==null){
            throw new ServiceException("经纬度不能为空");
        }
        Signin convert = MapstructUtils.convert(req, Signin.class);
        convert.setCreatorId(loginHelper.getCurrentAppUserId());
        signinMapper.insert(convert);
    }

    @Override
    public void executeSignin(SigninRecordReq req) {
        //todo 等待实现中。。

    }


}
