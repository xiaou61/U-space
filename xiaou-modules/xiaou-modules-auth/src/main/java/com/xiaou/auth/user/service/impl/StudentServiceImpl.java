package com.xiaou.auth.user.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaou.auth.user.domain.entity.Student;
import com.xiaou.auth.user.domain.req.StudentRegisterReq;
import com.xiaou.auth.user.mapper.StudentMapper;
import com.xiaou.auth.user.service.StudentService;
import com.xiaou.common.domain.R;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student>
    implements StudentService {

    @Override
    public R<String> register(StudentRegisterReq req) {
        return null;
    }

}




