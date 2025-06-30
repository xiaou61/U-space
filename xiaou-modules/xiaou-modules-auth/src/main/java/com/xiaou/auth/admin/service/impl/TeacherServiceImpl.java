package com.xiaou.auth.admin.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.auth.admin.domain.entity.AdminUser;
import com.xiaou.auth.admin.domain.entity.ClassEntity;
import com.xiaou.auth.admin.domain.entity.Teacher;
import com.xiaou.auth.admin.domain.req.TeacherLoginReq;
import com.xiaou.auth.admin.domain.req.TeacherReq;
import com.xiaou.auth.admin.domain.resp.TeacherResp;
import com.xiaou.auth.admin.mapper.AdminUserMapper;
import com.xiaou.auth.admin.mapper.TeacherMapper;
import com.xiaou.auth.admin.service.TeacherService;
import com.xiaou.common.domain.R;
import com.xiaou.common.page.PageReqDto;
import com.xiaou.common.page.PageRespDto;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.common.utils.PasswordUtil;
import com.xiaou.satoken.utils.LoginHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.xiaou.satoken.constant.RoleConstant.TEACHER;

@Service
@Slf4j
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher>
        implements TeacherService {

    @Resource
    private AdminUserMapper adminUserMapper;
    @Resource
    private LoginHelper loginHelper;

    @Override
    @Transactional
    public R<String> add(TeacherReq req) {
        Teacher convert = MapstructUtils.convert(req, Teacher.class);
        //默认密码为手机号后六位
        convert.setPassword(PasswordUtil.getEncryptPassword(convert.getPhone().substring(convert.getPhone().length() - 6)));
        baseMapper.insert(convert);
        //给教师加入到管理员里面不过她的角色为教师
        AdminUser adminUser = new AdminUser();
        adminUser.setUsername(convert.getEmployeeNo());
        adminUser.setPassword(convert.getPassword());
        adminUserMapper.insert(adminUser);
        //设置角色
        loginHelper.addUserRole(adminUser.getId(), TEACHER);
        return R.ok("添加成功");
    }

    @Override
    @Transactional
    public R<String> delete(String id) {
        // 先查
        Teacher teacher = baseMapper.selectById(id);

        // 防止 teacher 为 null
        if (teacher == null) {
            return R.fail("教师信息不存在，无法删除");
        }

        // 删除教师
        baseMapper.deleteById(id);

        // 删除教师账号与权限
        loginHelper.deleteUserRole(id, TEACHER);
        adminUserMapper.delete(new QueryWrapper<AdminUser>().eq("username", teacher.getEmployeeNo()));

        return R.ok("删除成功");
    }


    @Override
    public R<PageRespDto<TeacherResp>> pageTeacher(PageReqDto req) {
        IPage<Teacher> page = new Page<>(req.getPageNum(), req.getPageSize());
        QueryWrapper<Teacher> queryWrapper = new QueryWrapper<>();
        IPage<Teacher> teacherIPage = baseMapper.selectPage(page, queryWrapper);
        return R.ok(PageRespDto.of(teacherIPage.getCurrent(),
                teacherIPage.getSize(),
                teacherIPage.getTotal(),
                MapstructUtils.convert(teacherIPage.getRecords(), TeacherResp.class)));
    }


}




