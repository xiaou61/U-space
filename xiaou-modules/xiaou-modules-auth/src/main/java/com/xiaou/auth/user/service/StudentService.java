package com.xiaou.auth.user.service;


import cn.dev33.satoken.util.SaResult;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaou.auth.user.domain.entity.Student;
import com.xiaou.auth.user.domain.req.StudentLoginReq;
import com.xiaou.auth.user.domain.req.StudentRegisterReq;
import com.xiaou.auth.user.domain.resp.StudentInfoResp;
import com.xiaou.auth.user.domain.resp.StudentLoginClassResp;
import com.xiaou.common.domain.R;

import java.util.List;

public interface StudentService extends IService<Student> {

    R<String> register(StudentRegisterReq req);

    R<SaResult> login(StudentLoginReq req);

    R<StudentInfoResp> getInfo();

    R<List<StudentLoginClassResp>> listAll();
}
