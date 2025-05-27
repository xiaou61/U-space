package com.xiaou.user.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.common.constant.GlobalConstants;
import com.xiaou.common.constant.UserConstant;
import com.xiaou.common.domain.R;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.common.utils.PasswordUtil;
import com.xiaou.mail.utils.MailUtils;
import com.xiaou.user.domain.bo.StudentUserBo;
import com.xiaou.user.domain.entity.StudentUser;
import com.xiaou.user.domain.vo.StudentUserVo;
import com.xiaou.user.mapper.StudentUserMapper;
import com.xiaou.user.service.StudentUserService;
import com.xiaou.utils.RedisUtils;
import org.springframework.data.repository.cdi.Eager;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service
public class StudentUserServiceImpl extends ServiceImpl<StudentUserMapper, StudentUser>
        implements StudentUserService {

    @Override
    public R<Map<String, Object>> login(StudentUserBo bo) {
        String studentNumber = bo.getStudentNumber();
        String password = PasswordUtil.getEncryptPassword(bo.getPassword());

        StudentUser studentUser = lambdaQuery()
                .eq(StudentUser::getStudentNumber, studentNumber)
                .eq(StudentUser::getPassword, password)
                .one();

        if (studentUser == null) {
            return R.fail("学号或密码错误");
        }

        StpUtil.login(studentUser.getId());
        StpUtil.getSession().set(UserConstant.APPUSERLOGIN, studentUser);

        Map<String, Object> result = new HashMap<>();
        result.put("token", StpUtil.getTokenValue());
        result.put("user", studentUser);

        return R.ok(result);
    }


    @Override
    public R<StudentUserVo> getLogin(String currentStudentId) {
        //根据id查询学生信息
        StudentUser studentUser = baseMapper.selectById(currentStudentId);
        StudentUserVo studentUserVo = MapstructUtils.convert(studentUser, StudentUserVo.class);
        return R.ok(studentUserVo);
    }

    @Override
    public R<String> uploadAvatar(String avatar) {
        StudentUser studentUser = baseMapper.selectById(StpUtil.getLoginIdAsLong());
        studentUser.setAvatarUrl(avatar);
        baseMapper.updateById(studentUser);
        //todo 头像上传审核
        return R.ok("头像上传成功");
    }

    @Override
    public R<String> resetPassword(String password) {
        String encryptPassword = PasswordUtil.getEncryptPassword(password);
        StudentUser studentUser = baseMapper.selectById(StpUtil.getLoginIdAsLong());
        studentUser.setPassword(encryptPassword);
        baseMapper.updateById(studentUser);
        return R.ok("密码修改成功");
    }

    @Override
    public R<String> bindEmail(String email, String code) {
        StudentUser studentUser = baseMapper.selectById(StpUtil.getLoginIdAsLong());
        String s = GlobalConstants.CAPTCHA_CODE_KEY + email;
        String redisCode = (String) RedisUtils.getCacheObject(s);
        if (redisCode == null) {
            return R.fail("验证码已过期");
        }
        if (!redisCode.equals(code)) {
            return R.fail("验证码错误");
        }
        studentUser.setEmail(email);
        baseMapper.updateById(studentUser);
        return R.ok("绑定成功");
    }

    @Override
    public R<String> forgetPassword(String email, String code, String password) {
        String s = GlobalConstants.CAPTCHA_CODE_KEY + email;
        String redisCode = (String) RedisUtils.getCacheObject(s);
        if (redisCode == null) {
            return R.fail("验证码已过期");
        }
        if (!redisCode.equals(code)) {
            return R.fail("验证码错误");
        }
        StudentUser studentUser = lambdaQuery().eq(StudentUser::getEmail, email).one();
        if (studentUser == null) {
            return R.fail("该邮箱未绑定");
        }
        studentUser.setPassword(PasswordUtil.getEncryptPassword(password));
        baseMapper.updateById(studentUser);
        return R.ok("密码重置成功");
    }
}




