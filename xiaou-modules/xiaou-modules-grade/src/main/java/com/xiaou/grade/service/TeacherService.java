package com.xiaou.grade.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.grade.domain.bo.TeacherBo;
import com.xiaou.grade.domain.entity.Teacher;
import com.xiaou.grade.domain.vo.TeacherVo;

/**
* @author Lenovo
* @description 针对表【u_teacher】的数据库操作Service
* @createDate 2025-06-19 14:50:58
*/
public interface TeacherService extends IService<Teacher> {

    TeacherVo addTeacher(TeacherBo bo);

    boolean updatePassword(String oldPassword, String newPassword);
}
