package com.xiaou.auth.user.service.impl;


import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.auth.admin.domain.entity.ClassEntity;
import com.xiaou.auth.admin.domain.resp.StudentInfoPageResp;
import com.xiaou.auth.admin.mapper.ClassMapper;
import com.xiaou.auth.user.constant.UserConstant;
import com.xiaou.auth.user.domain.entity.StudentEntity;
import com.xiaou.auth.user.domain.mq.StudentMsgMQ;
import com.xiaou.auth.user.domain.req.StudentLoginReq;
import com.xiaou.auth.user.domain.req.StudentRegisterReq;
import com.xiaou.auth.user.domain.resp.StudentInfoResp;
import com.xiaou.auth.user.domain.resp.StudentLoginClassResp;
import com.xiaou.auth.user.mapper.StudentMapper;
import com.xiaou.auth.user.service.StudentService;
import com.xiaou.common.constant.GlobalConstants;
import com.xiaou.common.domain.R;
import com.xiaou.common.exception.ServiceException;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.common.utils.PasswordUtil;
import com.xiaou.common.utils.StringUtils;
import com.xiaou.mq.utils.RabbitMQUtils;
import com.xiaou.satoken.utils.LoginHelper;
import com.xiaou.upload.utils.FilesUtils;
import jakarta.annotation.Resource;
import org.dromara.x.file.storage.core.FileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;



@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, StudentEntity>
        implements StudentService {

    @Resource
    private RabbitMQUtils rabbitMQUtils;
    @Autowired
    private LoginHelper loginHelper;
    @Resource
    private ClassMapper classmapper;
    @Resource
    private FilesUtils filesUtils;

    @Override
    public R<String> register(StudentRegisterReq req) {
        StudentEntity studentEntity = MapstructUtils.convert(req, StudentEntity.class);
        studentEntity.setPassword(PasswordUtil.getEncryptPassword(studentEntity.getPassword()));
        baseMapper.insert(studentEntity);
        //发送MQ消息给管理员 这个为当前用户所在班级的导员
        StudentMsgMQ studentMsgMQ = new StudentMsgMQ();
        StudentMsgMQ mq = MapstructUtils.convert(studentEntity, studentMsgMQ);
        rabbitMQUtils.sendEmailMessage(mq);
        return R.ok("注册成功");

    }

    @Override
    public R<SaResult> login(StudentLoginReq req) {
        StudentEntity studentEntity = BeanUtil.copyProperties(req, StudentEntity.class);
        QueryWrapper<StudentEntity> queryWrapper = new QueryWrapper<>();
        //看看学号是否存在
        queryWrapper.eq(UserConstant.STUDENT_NO, studentEntity.getStudentNo());
        StudentEntity one = this.getOne(queryWrapper);
        if (one == null) {
            return R.fail("用户不存在");
        }
        //如果存在判断状态
        if (one.getStatus() == GlobalConstants.ZERO) {
            return R.fail("用户暂未被管理员审核成功");
        }
        //校验密码
        String password = PasswordUtil.getEncryptPassword(studentEntity.getPassword());
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
        StudentEntity studentEntity = baseMapper.selectById(currentAppUserId);
        StudentInfoResp convert = BeanUtil.copyProperties(studentEntity, StudentInfoResp.class);
        //填充班级名称
        //todo 这里可以优化
        convert.setClassName(classmapper.selectById(convert.getClassId()).getClassName());
        return R.ok(convert);
    }

    @Override
    public R<List<StudentLoginClassResp>> listAll() {
        //查询所有的班级
        List<ClassEntity> classList = classmapper.selectList(null);
        return R.ok(MapstructUtils.convert(classList, StudentLoginClassResp.class));
    }

    @Override
    public R<PageRespDto<StudentInfoPageResp>> pageAll(PageReqDto req) {
        IPage<StudentEntity> page = new Page<>(req.getPageNum(), req.getPageSize());
        QueryWrapper<StudentEntity> queryWrapper = new QueryWrapper<>();
        IPage<StudentEntity> studentIPage = baseMapper.selectPage(page, queryWrapper);
        List<StudentInfoPageResp> convert = MapstructUtils.convert(studentIPage.getRecords(), StudentInfoPageResp.class);
        convert.forEach(studentInfoPageResp -> studentInfoPageResp.setName(classmapper.selectById(studentInfoPageResp.getClassId()).getClassName()));
        return R.ok(PageRespDto.of(studentIPage.getCurrent(),
                studentIPage.getSize(),
                studentIPage.getTotal(),
                convert
        ));
    }

    @Override
    public R<List<StudentInfoPageResp>> pageUnAudited() {
        //查看全部status为0的
        QueryWrapper<StudentEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(UserConstant.STATUS, GlobalConstants.ZERO);
        //查询全部并且返回
        List<StudentInfoPageResp> convert = MapstructUtils.convert(baseMapper.selectList(queryWrapper), StudentInfoPageResp.class);
        //查询name
        convert.forEach(studentInfoPageResp -> studentInfoPageResp.setName(classmapper.selectById(studentInfoPageResp.getClassId()).getClassName()));
        return R.ok(convert);
    }

    @Override
    public R<String> audit(String id) {
        StudentEntity studentEntity = baseMapper.selectById(id);
        if (studentEntity == null) {
            return R.fail("用户不存在");
        }
        studentEntity.setStatus(GlobalConstants.ONE);
        baseMapper.updateById(studentEntity);
        //TODO 给学生发送信息提醒
        return R.ok("成功");
    }

    @Override
    public R<String> updateAvatar(String avatar) {
        //获得当前登录对象
        String currentAppUserId = loginHelper.getCurrentAppUserId();
        StudentEntity studentEntity = baseMapper.selectById(currentAppUserId);
        studentEntity.setAvatar(avatar);
        baseMapper.updateById(studentEntity);
        return R.ok("更换头像成功");
    }

    @Override
    public R<String> updatePassword(String oldPassword, String newPassword) {
        StudentEntity studentEntity = baseMapper.selectById(loginHelper.getCurrentAppUserId());
        if (!StringUtils.equals(PasswordUtil.getEncryptPassword(oldPassword), studentEntity.getPassword())) {
            return R.fail("旧密码错误");
        }
        studentEntity.setPassword(PasswordUtil.getEncryptPassword(newPassword));
        baseMapper.updateById(studentEntity);
        return R.ok("修改密码成功");
    }

    @Override
    public R<String> uploadAvatar(MultipartFile file) {
        try {
            //这里检测图片是否违规 需要传入一些值 注释打开就可以用
//            AliGreenImageScanUtil aliGreenImageScanUtil = new AliGreenImageScanUtil();
            FileInfo fileInfo = filesUtils.uploadFile(file);
//            aliGreenImageScanUtil.scanImage(fileInfo.getUrl());
            return R.ok("上传成功", fileInfo.getUrl());
        } catch (Exception e) {
            throw new ServiceException("头像上传失败");
        }
    }

    @Override
    public R<String> logout() {
        StpUtil.logout(loginHelper.getCurrentAppUserId());
        return R.ok("注销成功");
    }
}




