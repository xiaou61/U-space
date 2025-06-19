package com.xiaou.grade.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.common.utils.MapstructUtils;
import com.xiaou.common.utils.PasswordUtil;
import com.xiaou.grade.domain.bo.TeacherBo;
import com.xiaou.grade.domain.entity.Teacher;
import com.xiaou.grade.domain.vo.TeacherVo;
import com.xiaou.grade.mapper.TeacherMapper;
import com.xiaou.grade.service.TeacherService;
import com.xiaou.utils.LoginHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher>
        implements TeacherService {

    @Override
    @Transactional
    public TeacherVo addTeacher(TeacherBo bo) {
        Teacher t = MapstructUtils.convert(bo, Teacher.class);
        String phone = t.getPhone();
        if (phone != null && phone.length() >= 6) {
            t.setPassword(PasswordUtil.getEncryptPassword(phone.substring(phone.length() - 6)));
        }
        if (save(t)) {
            //添加权限
            LoginHelper.addUserRole(t.getId(), "teacher");
            return MapstructUtils.convert(t, TeacherVo.class);
        }
        return null;
    }

    @Override
    public boolean updatePassword(String oldPassword, String newPassword) {
        Teacher teacher = getBaseMapper().selectById(LoginHelper.getCurrentAppUserId());
        if (PasswordUtil.getEncryptPassword(oldPassword).equals(teacher.getPassword())) {
            teacher.setPassword(PasswordUtil.getEncryptPassword(newPassword));
            return updateById(teacher);
        }
        return false;
    }
}




