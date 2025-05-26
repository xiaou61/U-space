package com.xiaou.user.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.common.constant.UserConstant;
import com.xiaou.common.domain.R;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.common.utils.PasswordUtil;
import com.xiaou.user.domain.bo.StudentUserBo;
import com.xiaou.user.domain.entity.StudentUser;
import com.xiaou.user.domain.vo.StudentUserVo;
import com.xiaou.user.mapper.StudentUserMapper;
import com.xiaou.user.service.StudentUserService;
import org.springframework.stereotype.Service;


@Service
public class StudentUserServiceImpl extends ServiceImpl<StudentUserMapper, StudentUser>
        implements StudentUserService {

    @Override
    public R<SaResult> login(StudentUserBo bo) {
        String studentNumber = bo.getStudentNumber();
        String password = PasswordUtil.getEncryptPassword(bo.getPassword());
        QueryWrapper<StudentUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_number", studentNumber);
        queryWrapper.eq("password", password);
        StudentUser studentUser = baseMapper.selectOne(queryWrapper);
        if (studentUser == null) {
            return R.fail("用户学号或密码错误");
        }
        StpUtil.login(studentUser.getId());
        // 4. 保存用户的登录态
        StpUtil.getSession().set(UserConstant.APPUSERLOGIN, studentUser);
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        return R.ok("登录成功",SaResult.data(tokenInfo));
    }

    @Override
    public R<StudentUserVo> getLogin(String currentStudentId) {
        //根据id查询学生信息
        StudentUser studentUser = baseMapper.selectById(currentStudentId);
        StudentUserVo studentUserVo = MapstructUtils.convert(studentUser, StudentUserVo.class);
        return R.ok(studentUserVo);
    }
}




