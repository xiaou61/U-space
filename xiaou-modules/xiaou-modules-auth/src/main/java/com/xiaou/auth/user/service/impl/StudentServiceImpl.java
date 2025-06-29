package com.xiaou.auth.user.service.impl;


import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.auth.admin.domain.entity.ClassEntity;
import com.xiaou.auth.admin.mapper.ClassMapper;
import com.xiaou.auth.admin.service.ClassService;
import com.xiaou.auth.user.constant.UserConstant;
import com.xiaou.auth.user.domain.entity.Student;
import com.xiaou.auth.user.domain.mq.StudentMsgMQ;
import com.xiaou.auth.user.domain.req.StudentLoginReq;
import com.xiaou.auth.user.domain.req.StudentRegisterReq;
import com.xiaou.auth.user.domain.resp.StudentInfoResp;
import com.xiaou.auth.user.domain.resp.StudentLoginClassResp;
import com.xiaou.auth.user.mapper.StudentMapper;
import com.xiaou.auth.user.service.StudentService;
import com.xiaou.common.constant.GlobalConstants;
import com.xiaou.common.domain.R;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.common.utils.PasswordUtil;
import com.xiaou.common.utils.StringUtils;
import com.xiaou.mq.config.RabbitMQConfig;
import com.xiaou.mq.utils.RabbitMQUtils;
import com.xiaou.satoken.utils.LoginHelper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student>
        implements StudentService {

    @Resource
    private RabbitMQUtils rabbitMQUtils;
    @Autowired
    private LoginHelper loginHelper;
    @Resource
    private ClassMapper classmapper;

    @Override
    public R<String> register(StudentRegisterReq req) {
        Student student = MapstructUtils.convert(req, Student.class);
        student.setPassword(PasswordUtil.getEncryptPassword(student.getPassword()));
        baseMapper.insert(student);
        //发送MQ消息给管理员 这个为当前用户所在班级的导员
        StudentMsgMQ studentMsgMQ = new StudentMsgMQ();
        StudentMsgMQ mq = MapstructUtils.convert(student, studentMsgMQ);
        rabbitMQUtils.sendEmailMessage(mq);
        return R.ok("注册成功");

    }

    @Override
    public R<SaResult> login(StudentLoginReq req) {
        Student student = MapstructUtils.convert(req, Student.class);
        QueryWrapper<Student> queryWrapper = new QueryWrapper<>();
        //看看学号是否存在
        queryWrapper.eq(UserConstant.STUDENT_NO, student.getStudentNo());
        Student one = this.getOne(queryWrapper);
        if (one == null) {
            return R.fail("用户不存在");
        }
        //如果存在判断状态
        if (one.getStatus() == GlobalConstants.ZERO) {
            return R.fail("用户暂未被管理员审核成功");
        }
        //校验密码
        String password = PasswordUtil.getEncryptPassword(student.getPassword());
        if (!StringUtils.equals(password, one.getPassword())) {
            return R.fail("密码错误");
        }
        //都成功的话使用sa-token进行登录
        StpUtil.login(one.getId());
        return R.ok(SaResult.data(StpUtil.getTokenInfo()));
    }

    @Override
    public R<StudentInfoResp> getInfo() {
        String currentAppUserId = loginHelper.getCurrentAppUserId();
        Student student = baseMapper.selectById(currentAppUserId);
        StudentInfoResp convert = MapstructUtils.convert(student, StudentInfoResp.class);
        return R.ok(convert);
    }

    @Override
    public R<List<StudentLoginClassResp>> listAll() {
        //查询所有的班级
        List<ClassEntity> classList = classmapper.selectList(null);
        return R.ok(MapstructUtils.convert(classList, StudentLoginClassResp.class));
    }
}




