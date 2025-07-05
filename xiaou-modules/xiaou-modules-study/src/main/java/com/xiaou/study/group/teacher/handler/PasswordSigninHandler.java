package com.xiaou.study.group.teacher.handler;

import com.xiaou.common.exception.ServiceException;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.satoken.utils.LoginHelper;
import com.xiaou.study.group.teacher.domain.entity.Signin;
import com.xiaou.study.group.teacher.domain.entity.SigninRecord;
import com.xiaou.study.group.teacher.domain.req.SigninRecordReq;
import com.xiaou.study.group.teacher.domain.req.SigninReq;
import com.xiaou.study.group.teacher.mapper.SigninMapper;
import com.xiaou.study.group.teacher.mapper.SigninRecordMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class PasswordSigninHandler implements SigninHandler{

    @Resource
    private SigninMapper signinMapper;

    @Resource
    private LoginHelper loginHelper;

    @Resource
    private SigninRecordMapper signinRecordMapper;
    @Override
    public boolean support(Integer type) {
        return type == 1;
    }

    //密码实现方式
    @Override
    public void handle(SigninReq req) {
        //判断是否有密码
        if (req.getPassword() == null){
            throw new ServiceException("签到密码不能为空");
        }
        //直接添加
        Signin convert = MapstructUtils.convert(req, Signin.class);
        convert.setCreatorId(loginHelper.getCurrentAppUserId());
        signinMapper.insert(convert);
    }

    @Override
    public void executeSignin(SigninRecordReq req) {
        //判断密码是否一致
        Signin signin = signinMapper.selectById(req.getSigninId());
        if (!signin.getPassword().equals(req.getPassword())){
            throw new ServiceException("签到密码错误");
        }
        SigninRecord convert = MapstructUtils.convert(req, SigninRecord.class);
        convert.setStudentId(loginHelper.getCurrentAppUserId());
        convert.setSigninTime(new Date());
        //判断是否迟到根据id查找当前的截止时间
        if (signin.getEndTime().getTime() < convert.getSigninTime().getTime()) {
            convert.setIsLate(1);
        }
        signinRecordMapper.insert(convert);
    }

}
