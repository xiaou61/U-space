package com.xiaou.auth.user.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.auth.user.domain.entity.Student;
import com.xiaou.auth.user.domain.req.StudentRegisterReq;
import com.xiaou.common.domain.R;

public interface StudentService extends IService<Student> {

    R<String> register(StudentRegisterReq req);
}
